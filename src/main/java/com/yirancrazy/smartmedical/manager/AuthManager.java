package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.CryptoException;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.RoleConstant;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.vo.LoginVo;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.PatientCardService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.SmsService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.NicknameGenerator;
import com.yirancrazy.smartmedical.utils.PasswordUtil;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yirancrazy.smartmedical.constant.RoleConstant.ROLE_LIST;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
@Slf4j
public class AuthManager {

    @Value("${jwt.accessSecretKey}")                       // 访问 jwt 加密密钥
    private String accessSecretKey;
    @Value("${jwt.refreshSecretKey}")                      // 刷新 jwt 加密密钥
    private String refreshSecretKey;
    @Value("${jwt.accessTokenPrefix}")
    private String accessTokenPrefix;                 // jwt 访问令牌前缀
    @Value("${jwt.admin.adminRefreshTokenPrefix}")
    private String adminRefreshTokenPrefix;                // 管理员 jwt 刷新加密密钥
    @Value("${cookie.secure:false}")
    private boolean cookieSecure;                          // 决定 cookie 是否只允许通过 https 传输
    private final AccountService accountService;
    private final UserService userService;
    private final RedisUtil redisUtil;
    private final PatientCardService patientCardService;
    private final PatientService patientService;
    private final SmsService smsService;
    private final Long USER_ROLE = 4L;
    /** S22: 登录限流窗口 5 分钟 */
    private static final long LOGIN_RATE_WINDOW_MINUTES = 5L;
    /** S22: 登录限流阈值 5 次/窗口 */
    private static final long LOGIN_RATE_MAX = 5L;

    /**
     * S22: 登录限流 — 同手机号 5 分钟内最多 5 次，超限抛 BizException
     * @param phone 手机号
     */
    private void checkLoginRate(String phone) {
        String key = "login:rate:user:" + phone;
        Long count = redisUtil.incrAndExpireOnFirst(key, 1L, LOGIN_RATE_WINDOW_MINUTES, TimeUnit.MINUTES);
        if (count != null && count > LOGIN_RATE_MAX) {
            log.warn("[login-rate] phone={} count={} 限流", phone, count);
            throw new BizException(BizErrorCode.LOGIN_RATE_LIMITED);
        }
    }

    /**
     * 用户登录
     * @param phone 用户手机号
     * @param password 用户密码
     * @param response HttpServletResponse 响应对象
     * @return 登录结果
     */
    public Result<LoginVo> login(String phone, String password, HttpServletResponse response) {
        // S21: 不再 catch Exception 吞掉系统异常，DB/Redis 等异常往上抛由 GlobalExceptionHandler 处理
        // S22: 登录限流
        checkLoginRate(phone);
        List<Account> accountByPhone = accountService.getAccountByPhone(phone);

            // 过滤出用户账户
            Account account = accountByPhone
                    .stream()
                    .filter(account1 -> USER_ROLE
                            .equals(account1.getRoleId()))
                    .findFirst()
                    .orElse(null);

            if(account==null){
                log.warn("[login] 账号不存在, phone={}", phone);
                return Result.info(10001,"账号不存在", null);
            }

            // 校验原始密码和加密后的密码
            if(!PasswordUtil.verify(password, account.getPassword())){
                log.warn("[login] 密码错误, phone={}", phone);
                return Result.info(10002,"用户名或密码错误", null);
            }

            // 历史弱密码（MD5/明文）登录后自动升级为 BCrypt，逐步消除弱密码存储
            if (PasswordUtil.needsBcryptUpgrade(account.getPassword())) {
                account.setPassword(PasswordUtil.encode(password));
                accountService.updateAccountById(account);
                log.info("[login] 密码已升级为 BCrypt, accountId={}", account.getId());
            }

            // 获取用户信息
            User user = userService.getUserById(account.getUserId());

            return Result.success(issueTokens(account, user, response));
    }

    /**
     * 发送短信验证码（未登录，60s 冷却防短信轰炸）
     * @param phone 用户手机号
     * @param scene 场景：login（验证码登录，账号必须已存在）/ register（注册，默认，账号不能已存在）
     * @return 发送结果
     */
    public Result<String> sendSmsCode(String phone, String scene) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式不正确");
        }
        boolean isLogin = "login".equalsIgnoreCase(scene);
        boolean isForgot = "forgot".equalsIgnoreCase(scene); // 忘记密码：账号必须已存在
        List<Account> existing = accountService.getAccountByPhone(phone);
        boolean exists = existing != null && !existing.isEmpty();
        if (isLogin || isForgot) {
            // 验证码登录：账号必须已存在，未注册提示去注册
            if (!exists) {
                return Result.info(10001, "账号不存在", null);
            }
        } else {
            // 注册：账号不能已存在
            if (exists) {
                return Result.info(10001, "账号已存在", null);
            }
        }
        // 冷却/发送失败由 SmsService 抛 BizException，GlobalExceptionHandler 统一转 Result
        smsService.sendCode(phone);
        return Result.success("验证码已发送");
    }

    /**
     * 用户验证码登录（手机号 + 短信验证码，登录成功签发 token）
     * @param phone 用户手机号
     * @param code 短信验证码
     * @param response HttpServletResponse 响应对象（用于签发 token）
     * @return 登录结果
     */
    public Result<LoginVo> loginByCode(String phone, String code, HttpServletResponse response) {
        checkLoginRate(phone);
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式不正确");
        }
        // 校验验证码（过期/错误抛 BizException，GlobalExceptionHandler 统一转 Result）
        smsService.verifyCode(phone, code);
        Account account = accountService.getAccountByPhone(phone)
                .stream()
                .filter(a -> USER_ROLE.equals(a.getRoleId()))
                .findFirst()
                .orElse(null);
        if (account == null) {
            log.warn("[login-by-code] 账号不存在, phone={}", phone);
            return Result.info(10001, "账号不存在", null);
        }
        User user = userService.getUserById(account.getUserId());
        return Result.success(issueTokens(account, user, response));
    }

    /**
     * 用户注册（手机号 + 短信验证码，注册成功自动登录）
     * @param phone 用户手机号
     * @param code 短信验证码
     * @param response HttpServletResponse 响应对象（用于签发 token）
     * @return 注册结果（自动登录返回 LoginVo）
     */
    @Transactional
    public Result<LoginVo> register(String phone, String code, HttpServletResponse response) {
        // 手机号格式校验
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式不正确");
        }
        // 校验验证码（过期/错误抛 BizException）
        smsService.verifyCode(phone, code);

        // 缓存查询结果避免重复调用
        List<Account> existing = accountService.getAccountByPhone(phone);
        if (existing != null && !existing.isEmpty()) {
            return Result.info(10001,"账号已存在", null);
        }

        // 生成用户
        User user = new User();
        user.setId(IdUtil.getSnowflakeNextId());
        user.setUsername(NicknameGenerator.generateRandomNickname());  // 生成随机昵称
        user.setAvatar("");   // todo 后续添加随机头像
        userService.insertUser(user);

        // 生成账号
        Account account = new Account();
        account.setId(IdUtil.getSnowflakeNextId());
        account.setUserId(user.getId());
        account.setRoleId(USER_ROLE);
        account.setPhone(phone);
        // 注册不设密码：写入不可猜的随机 BCrypt 占位，之后可通过「忘记密码」流程设置登录密码
        account.setPassword(PasswordUtil.encode(RandomUtil.randomString(16)));
        try {
            accountService.insertAccount(account);
        } catch (DuplicateKeyException e) {
            // 并发同手机号注册时由唯一索引兜底，抛出业务异常触发事务回滚，避免残留孤儿 User
            log.warn("[register] 手机号重复注册竞态 phone={}, roleId={}", phone, account.getRoleId());
            throw new BizException(BizErrorCode.ACCOUNT_ALREADY_EXISTS);
        }

        registerInit(user.getId());

        return Result.success(issueTokens(account, user, response));
    }

    /**
     * 用户登出：删除 Redis 中的 access/refresh token，使旧 token 立即失效（Filter 会比对 Redis）
     * @param accountId 账号ID（JWT sub，与 login 时 Redis key 一致）
     * @return 登出结果
     */
    public Result<String> logout(Long accountId) {
        redisUtil.delete(accessTokenPrefix + accountId);
        redisUtil.delete(adminRefreshTokenPrefix + accountId);
        return Result.success("登出成功");
    }

    /**
     * 用户忘记密码重置（未登录，须短信验证码校验，防止任意账号被接管）
     * @param phone 用户手机号
     * @param code 短信验证码
     * @param newPassword 新密码
     * @return 重置结果
     */
    public Result<String> forgotPassword(String phone, String code, String newPassword) {
        checkLoginRate(phone);
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式不正确");
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.fail("密码长度至少 6 位");
        }
        // 校验短信验证码（过期/错误抛 BizException），杜绝无身份校验的改密接管
        smsService.verifyCode(phone, code);
        Account account = accountService.getAccountByPhone(phone)
                .stream()
                .filter(a -> USER_ROLE.equals(a.getRoleId()))
                .findFirst()
                .orElse(null);
        if (account == null) {
            return Result.info(10001, "账号不存在", null);
        }
        // 直接以 BCrypt 覆盖，消除历史弱密码
        account.setPassword(PasswordUtil.encode(newPassword));
        accountService.updateAccountById(account);
        // 清理该账号所有 token，强制旧会话失效、重新登录
        redisUtil.delete(accessTokenPrefix + account.getId());
        redisUtil.delete(adminRefreshTokenPrefix + account.getId());
        log.info("[forgot-password] 密码已重置, accountId={}", account.getId());
        return Result.success("重置成功");
    }

    /**
     * 用户登录态修改密码，校验原密码后更新
     * @param accountId 账号ID
     * @param oldPassword 原密码
     * @param newPassword 新密码
     * @return 修改结果
     */
    public Result<String> changePassword(Long accountId, String oldPassword, String newPassword) {
        Account account = accountService.getAccountById(accountId);
        if (account == null) {
            return Result.info(10001, "账号不存在", null);
        }
        if (!PasswordUtil.verify(oldPassword, account.getPassword())) {
            log.warn("[change-password] 原密码错误, accountId={}", accountId);
            return Result.info(10002, "原密码不正确", null);
        }
        if (newPassword == null || newPassword.length() < 6) {
            return Result.fail("新密码长度至少 6 位");
        }
        account.setPassword(PasswordUtil.encode(newPassword));
        accountService.updateAccountById(account);
        // 强制重新登录：清除 Redis 中该账号的全部 token
        redisUtil.delete(accessTokenPrefix + accountId);
        redisUtil.delete(adminRefreshTokenPrefix + accountId);
        log.info("[change-password] 密码已修改, accountId={}", accountId);
        return Result.success("密码修改成功");
    }

    /**
     * 刷新 access token（消费 Refresh-token Cookie）
     * @param refreshToken Cookie 中的 refresh JWT
     * @param response 用于写入新 Authorization header
     * @return 新 access JWT 或失败信息
     */
    public Result<String> refresh(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return Result.fail("Refresh token 缺失");
        }
        try {
            JWT jwt = JWTUtil.parseToken(refreshToken);
            jwt.setKey(refreshSecretKey.getBytes());
            if (!jwt.verify()) {
                return Result.fail("Refresh token 无效");
            }
            JWTPayload payload = jwt.getPayload();
            String accountId = String.valueOf(payload.getClaim("sub"));

            // 从refresh token直接解析role，不依赖旧access token
            Long roleId;
            try {
                Object roleObj = payload.getClaim("role");
                roleId = roleObj != null ? Long.parseLong(String.valueOf(roleObj)) : 4L;
            } catch (NumberFormatException e) {
                roleId = 4L; // 默认user
            }

            // 角色变更后 refresh 必须以 DB 当前角色为准，防止降权后 30 天内仍持旧权限
            try {
                Account account = accountService.getAccountById(Long.parseLong(accountId));
                if (account == null) {
                    return Result.fail("账号不存在");
                }
                if (account.getRoleId() == null || !account.getRoleId().equals(roleId)) {
                    // 角色已变更，旧 refresh token 不再可信，清除并要求重新登录
                    redisUtil.delete(adminRefreshTokenPrefix + accountId);
                    redisUtil.delete(accessTokenPrefix + accountId);
                    return Result.fail("账号角色已变更，请重新登录");
                }
            } catch (NumberFormatException e) {
                return Result.fail("Refresh token 无效");
            }

            // 从refresh token解析userId，用于生成新access token
            Long userId;
            try {
                Object userIdObj = payload.getClaim("userId");
                userId = userIdObj != null ? Long.parseLong(String.valueOf(userIdObj)) : Long.parseLong(accountId);
            } catch (NumberFormatException e) {
                userId = Long.parseLong(accountId);
            }

            Long exp;
            try {
                Object expObj = payload.getClaim("exp");
                if (expObj == null) {
                    return Result.fail("Refresh token 无效");
                }
                exp = Long.parseLong(String.valueOf(expObj));
            } catch (NumberFormatException e) {
                return Result.fail("Refresh token 无效");
            }
            if (exp < System.currentTimeMillis() / 1000) {
                return Result.fail("Refresh token 已过期");
            }
            // Redis 比对：统一用 adminRefreshTokenPrefix + accountId（所有角色共用）
            String redisRefresh = redisUtil.get(adminRefreshTokenPrefix + accountId);
            if (redisRefresh == null || !redisRefresh.equals(refreshToken)) {
                return Result.fail("Refresh token 已失效");
            }
            // 签发新 access JWT（统一30分钟有效期）
            Long currentTimeSeconds = System.currentTimeMillis() / 1000;
            String newAccessJwt = generateAccessJwt(accountId, userId, roleId, currentTimeSeconds);

            // 覆盖旧 access（旧 token 立即失效）
            redisUtil.setEx(accessTokenPrefix + accountId, newAccessJwt, 30, TimeUnit.MINUTES);

            response.setHeader("Authorization", "Bearer " + newAccessJwt);
            return Result.success(newAccessJwt);
        } catch (CryptoException | DataAccessException | IllegalArgumentException e) {
            log.error("[refresh] 刷新token异常", e);
            return Result.fail("刷新token失败");
        }
    }

    /**
     * 签发访问/刷新 token 并写入响应头与 Cookie，返回登录信息（登录、注册自动登录共用）
     * @param account 账号
     * @param user 用户
     * @param response 响应对象
     * @return 登录信息
     */
    private LoginVo issueTokens(Account account, User user, HttpServletResponse response) {
        Long currentTimeSeconds = System.currentTimeMillis() / 1000;
        String accessJwt = generateAccessJwt(account.getId().toString(), account.getUserId(), account.getRoleId(), currentTimeSeconds);
        // 存储JWT访问令牌（统一前缀用于所有角色，统一管理）
        redisUtil.setEx(accessTokenPrefix + account.getId(), accessJwt, 30, TimeUnit.MINUTES);

        String refreshJwt = generateRefreshJwt(account.getId().toString(), account.getUserId(), account.getRoleId(), currentTimeSeconds);
        // 存储JWT刷新令牌（admin前缀用于所有角色，统一管理）
        redisUtil.setEx(adminRefreshTokenPrefix + account.getId(), refreshJwt, 30, TimeUnit.DAYS);

        LoginVo loginVo = new LoginVo(String.valueOf(account.getId()), accessJwt, String.valueOf(user.getId()), account.getPhone(), user.getNickname());

        // 统一通过响应头返回access token，前端从Authorization头提取
        response.setHeader("Authorization", "Bearer " + accessJwt);

        // 设置 Refresh-token Cookie，带回jwt 刷新token
        Cookie cookie = new Cookie("Refresh-token", refreshJwt);
        cookie.setMaxAge(30 * 24 * 60 * 60);
        cookie.setPath("/api");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        // SameSite=None 必须配合 Secure=true（HTTPS），开发环境（HTTP）不设置让浏览器用默认 Lax
        if (cookieSecure) {
            cookie.setAttribute("SameSite", "None");
        }
        response.addCookie(cookie);
        return loginVo;
    }

    /**
     * 生成访问JWT（30分钟有效期），exp 使用秒级 Unix 时间戳符合 JWT 标准
     */
    private String generateAccessJwt(String accountId, Long userId, Long roleId, Long currentTimeSeconds) {
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new HashMap<>();
        payload.put(JWTPayload.ISSUER, "YiRanCrazy");
        payload.put(JWTPayload.SUBJECT, accountId);
        payload.put("userId", userId);
        payload.put("role", roleId);
        payload.put(JWTPayload.EXPIRES_AT, currentTimeSeconds + 30 * 60);
        payload.put(JWTPayload.NOT_BEFORE, currentTimeSeconds);
        payload.put(JWTPayload.ISSUED_AT, currentTimeSeconds);
        payload.put(JWTPayload.JWT_ID, String.valueOf(IdUtil.getSnowflakeNextId()));

        return JWTUtil.createToken(header, payload, accessSecretKey.getBytes());
    }

    /**
     * 生成刷新JWT（30天有效期，包含role信息），exp 使用秒级 Unix 时间戳符合 JWT 标准
     */
    private String generateRefreshJwt(String accountId, Long userId, Long roleId, Long currentTimeSeconds) {
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new HashMap<>();
        payload.put(JWTPayload.ISSUER, "YiRanCrazy");
        payload.put(JWTPayload.SUBJECT, accountId);
        payload.put("userId", userId);
        payload.put("role", roleId); // refresh token包含role，刷新时直接解析
        payload.put(JWTPayload.EXPIRES_AT, currentTimeSeconds + 30L * 24 * 60 * 60);
        payload.put(JWTPayload.NOT_BEFORE, currentTimeSeconds);
        payload.put(JWTPayload.ISSUED_AT, currentTimeSeconds);
        payload.put(JWTPayload.JWT_ID, String.valueOf(IdUtil.getSnowflakeNextId()));

        return JWTUtil.createToken(header, payload, refreshSecretKey.getBytes());
    }

    private void registerInit(Long userId){
        PatientCard patientCard = new PatientCard();
        patientCard.setId(IdUtil.getSnowflakeNextId());
        patientCard.setSn(IdUtil.getSnowflakeNextId());
        patientCard.setInpatientBalance(0);
        patientCard.setOutpatientBalance(0);
        patientCard.setPaymentPassword("");
        patientCard.setQrCode("");
        patientCard.setStatus(0);
        patientCardService.insertPatientCard(patientCard);

        Patient patient = new Patient();
        patient.setId(IdUtil.getSnowflakeNextId());
        patient.setUserId(userId);
        patient.setPatientCardId(patientCard.getId());
        patientService.insertPatient(patient);
    }

}
