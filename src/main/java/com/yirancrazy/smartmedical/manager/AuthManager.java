package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
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
    @Value("${jwt.admin.adminAccessTokenPrefix}")
    private String adminAccessTokenPrefix;                 // 管理员 jwt 访问加密密钥
    @Value("${jwt.admin.adminRefreshTokenPrefix}")
    private String adminRefreshTokenPrefix;                // 管理员 jwt 刷新加密密钥
    @Value("${cookie.secure:false}")
    private boolean cookieSecure;                          // 决定 cookie 是否只允许通过 https 传输
    private final AccountService accountService;
    private final UserService userService;
    private final RedisUtil redisUtil;
    private final PatientCardService patientCardService;
    private final PatientService patientService;
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

            // 生成JWT访问令牌
            Long currentTimeMillis = System.currentTimeMillis();
            String accessJwt = generateAccessJwt(account.getId().toString(), account.getUserId(), account.getRoleId(), currentTimeMillis);

            // 存储JWT访问令牌（admin前缀用于所有角色，统一管理）
            redisUtil.setEx(adminAccessTokenPrefix + account.getId().toString(), accessJwt, 30, TimeUnit.MINUTES);

            // 生成JWT 刷新令牌（包含role信息，刷新时直接解析）
            String refreshJwt = generateRefreshJwt(account.getId().toString(), account.getUserId(), account.getRoleId(), currentTimeMillis);

            // 存储JWT刷新令牌（admin前缀用于所有角色，统一管理）
            redisUtil.setEx(adminRefreshTokenPrefix+account.getId().toString(),refreshJwt,30, TimeUnit.DAYS);

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

            return Result.success(loginVo);
    }

    /**
     * 用户注册
     * @param phone 用户手机号
     * @param password 用户密码
     * @return 注册结果
     */
    @Transactional
    public Result<String> register(String phone, String password) {
        // S20: 手机号格式校验
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            return Result.fail("手机号格式不正确");
        }
        if (password == null || password.length() < 6) {
            return Result.fail("密码长度至少 6 位");
        }
        // S20: 缓存查询结果避免重复调用
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
        account.setPassword(PasswordUtil.encode(password));
        accountService.insertAccount(account);

        registerInit(user.getId());

        return Result.success("注册成功");
    }

    /**
     * 用户登出：删除 Redis 中的 access/refresh token，使旧 token 立即失效（Filter 会比对 Redis）
     * @param accountId 账号ID（JWT sub，与 login 时 Redis key 一致）
     * @return 登出结果
     */
    public Result<String> logout(Long accountId) {
        redisUtil.delete(adminAccessTokenPrefix + accountId);
        redisUtil.delete(adminRefreshTokenPrefix + accountId);
        return Result.success("登出成功");
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
            } catch (Exception e) {
                roleId = 4L; // 默认user
            }

            // 从refresh token解析userId，用于生成新access token
            Long userId;
            try {
                Object userIdObj = payload.getClaim("userId");
                userId = userIdObj != null ? Long.parseLong(String.valueOf(userIdObj)) : Long.parseLong(accountId);
            } catch (Exception e) {
                userId = Long.parseLong(accountId);
            }

            long exp = Long.parseLong(String.valueOf(payload.getClaim("exp")));
            if (exp < System.currentTimeMillis()) {
                return Result.fail("Refresh token 已过期");
            }
            // Redis 比对：统一用 adminRefreshTokenPrefix + accountId（所有角色共用）
            String redisRefresh = redisUtil.get(adminRefreshTokenPrefix + accountId);
            if (redisRefresh == null || !redisRefresh.equals(refreshToken)) {
                return Result.fail("Refresh token 已失效");
            }

            // 签发新 access JWT（统一30分钟有效期）
            Long currentTimeMillis = System.currentTimeMillis();
            String newAccessJwt = generateAccessJwt(accountId, userId, roleId, currentTimeMillis);

            // 覆盖旧 access（旧 token 立即失效）
            redisUtil.setEx(adminAccessTokenPrefix + accountId, newAccessJwt, 30, TimeUnit.MINUTES);

            response.setHeader("Authorization", "Bearer " + newAccessJwt);
            return Result.success(newAccessJwt);
        } catch (Exception e) {
            log.error("[refresh] 刷新token异常", e);
            return Result.fail("刷新token失败");
        }
    }

    /**
     * 生成访问JWT（30分钟有效期）
     */
    private String generateAccessJwt(String accountId, Long userId, Long roleId, Long currentTimeMillis) {
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new HashMap<>();
        payload.put(JWTPayload.ISSUER, "YiRanCrazy");
        payload.put(JWTPayload.SUBJECT, accountId);
        payload.put("userId", userId);
        payload.put("role", roleId);
        payload.put(JWTPayload.EXPIRES_AT, currentTimeMillis + 1000L * 60 * 30);
        payload.put(JWTPayload.NOT_BEFORE, currentTimeMillis);
        payload.put(JWTPayload.ISSUED_AT, currentTimeMillis);
        payload.put(JWTPayload.JWT_ID, String.valueOf(IdUtil.getSnowflakeNextId()));

        return JWTUtil.createToken(header, payload, accessSecretKey.getBytes());
    }

    /**
     * 生成刷新JWT（30天有效期，包含role信息）
     */
    private String generateRefreshJwt(String accountId, Long userId, Long roleId, Long currentTimeMillis) {
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new HashMap<>();
        payload.put(JWTPayload.ISSUER, "YiRanCrazy");
        payload.put(JWTPayload.SUBJECT, accountId);
        payload.put("userId", userId);
        payload.put("role", roleId); // refresh token包含role，刷新时直接解析
        payload.put(JWTPayload.EXPIRES_AT, currentTimeMillis + 1000L * 60 * 60 * 24 * 30);
        payload.put(JWTPayload.NOT_BEFORE, currentTimeMillis);
        payload.put(JWTPayload.ISSUED_AT, currentTimeMillis);
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
