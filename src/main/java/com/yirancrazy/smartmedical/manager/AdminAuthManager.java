package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.RoleConstant;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Admin;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.Role;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminResponseSimple;
import com.yirancrazy.smartmedical.manager.loader.impl.RoleTypeLoaderManage;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.AdminService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.RoleService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.PasswordUtil;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员认证管理层
 * @Datetime: 2026-03-05 17:14
 * @Version: 1.0
 */


@Manager
@RequiredArgsConstructor
@Slf4j
public class AdminAuthManager {

    @Value("${jwt.accessTokenPrefix}")
    private String accessTokenPrefix;   // 固定登录令牌前缀
    @Value("${jwt.admin.adminRefreshTokenPrefix}")
    private String adminRefreshTokenPrefix;   // 固定管理员刷新令牌前缀
    @Value("${jwt.accessSecretKey}")
    private String accessSecretKey;
    @Value("${jwt.refreshSecretKey}")
    private String refreshSecretKey;
    @Value("${cookie.secure:false}")
    private boolean cookieSecure;

    private final AccountService accountService;
    private final AdminService adminService;
    private final DoctorService doctorService;
    private final UserService userService;
    private final RoleService roleService;
    private final RedisUtil redisUtil;
    private final RoleTypeLoaderManage roleTypeLoaderManage;

    private Role adminRole;
    /** S22: 登录限流窗口 5 分钟 */
    private static final long LOGIN_RATE_WINDOW_MINUTES = 5L;
    /** S22: 登录限流阈值 5 次/窗口 */
    private static final long LOGIN_RATE_MAX = 5L;

    /**
     * S22: 登录限流 — 同手机号 5 分钟内最多 5 次，超限抛 BizException
     * @param phone 手机号
     */
    private void checkLoginRate(String phone) {
        String key = "login:rate:admin:" + phone;
        Long count = redisUtil.incrAndExpireOnFirst(key, 1L, LOGIN_RATE_WINDOW_MINUTES, TimeUnit.MINUTES);
        if (count != null && count > LOGIN_RATE_MAX) {
            log.warn("[login-rate] phone={} count={} 限流", phone, count);
            throw new BizException(BizErrorCode.LOGIN_RATE_LIMITED);
        }
    }

    /**
     * 启动时校验 JWT 密钥与 token 前缀已注入，避免运行时 NPE
     */
    @PostConstruct
    public void validateJwtConfig() {
        if (accessSecretKey == null || accessSecretKey.isBlank()) {
            throw new IllegalStateException("jwt.accessSecretKey 未配置");
        }
        if (refreshSecretKey == null || refreshSecretKey.isBlank()) {
            throw new IllegalStateException("jwt.refreshSecretKey 未配置");
        }
        if (accessTokenPrefix == null || accessTokenPrefix.isBlank()) {
            throw new IllegalStateException("jwt.accessTokenPrefix 未配置");
        }
        if (adminRefreshTokenPrefix == null || adminRefreshTokenPrefix.isBlank()) {
            throw new IllegalStateException("jwt.admin.adminRefreshTokenPrefix 未配置");
        }
        this.adminRole = RoleConstant.ROLE_LIST
                .stream()
                .filter(role -> role.getId().equals(1L))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("未找到系统管理员角色"));
    }
    /**
     * 管理员手机号密码登录
     * @param phone 手机号
     * @param password 密码
     * @param remember 是否记住
     * @param request 请求
     * @param response 响应
     * @return 结果
     */
    public Result<String> loginByPhoneAndPassword(String phone, String password,Boolean remember, HttpServletRequest request, HttpServletResponse response){
        return loginByPhoneAndPasswordAndRoleId(phone, password, adminRole.getId(), remember, request, response);
    }

    /**
     * 按指定角色登录（医生/药师/管理员共用）
     * @param phone 手机号
     * @param password 密码
     * @param roleId 期望角色ID
     * @param remember 是否记住
     * @param request 请求
     * @param response 响应
     * @return 结果
     */
    public Result<String> loginByPhoneAndPasswordAndRoleId(String phone, String password, Long roleId, Boolean remember, HttpServletRequest request, HttpServletResponse response) {
        // S22: 登录限流
        checkLoginRate(phone);
        List<Account> accountByPhone = accountService.getAccountByPhone(phone);

        // 查找出指定角色的账号
        Account roleAccount = accountByPhone
                .stream()
                .filter(account -> account.getRoleId().equals(roleId))
                .findFirst()
                .orElse(null);

        if (roleAccount == null) {
            return Result.fail("账号不存在或无权限");
        }
        if (Boolean.FALSE.equals(roleAccount.getEnabled())) {
            log.warn("[admin-login] 账号已禁用, phone={}", phone);
            return Result.fail("账号已被禁用，请联系管理员");
        }
        if (!PasswordUtil.verify(password, roleAccount.getPassword())) {
            return Result.fail("用户名或密码错误");
        }

        // B06: 医生角色登录时校验 doctor 表存在 id=account.userId 的记录，
        // 确保 JwtAuthenticationFilter 设的 currentDoctorId=userId 真等于 doctor.id，
        // 避免账号-医生档案错配导致开方/叫号命中错误医生
        if (Long.valueOf(2L).equals(roleAccount.getRoleId()) && roleAccount.getUserId() != null) {
            Doctor doctor = doctorService.getDoctorById(roleAccount.getUserId());
            if (doctor == null) {
                log.warn("[admin-login] 医生账号 userId={} 在 doctor 表无对应记录，拒绝登录", roleAccount.getUserId());
                return Result.fail("医生档案不存在，请联系管理员");
            }
        }

        // 历史弱密码（MD5/明文）登录后自动升级为 BCrypt，逐步消除弱密码存储
        if (PasswordUtil.needsBcryptUpgrade(roleAccount.getPassword())) {
            roleAccount.setPassword(PasswordUtil.encode(password));
            accountService.updateAccountById(roleAccount);
            log.info("[admin-login] 密码已升级为 BCrypt, accountId={}", roleAccount.getId());
        }

        // accessJWT 和 refreshJwt 写入 redis 中（统一前缀用于所有角色，统一管理）
        Long currentTimeMillis = System.currentTimeMillis();
        String accessJwt = createAccessJwt(roleAccount.getId().toString(), roleAccount.getUserId(), roleAccount.getRoleId(), currentTimeMillis);
        String refreshJwt = createRefreshJwt(roleAccount.getId().toString(), roleAccount.getUserId(), roleAccount.getRoleId(), currentTimeMillis);
        redisUtil.setEx(accessTokenPrefix + roleAccount.getId(), accessJwt, 30, TimeUnit.MINUTES);
        redisUtil.setEx(adminRefreshTokenPrefix + roleAccount.getId(), refreshJwt, 30, TimeUnit.DAYS);

        // 统一通过响应头返回access token，前端从Authorization头提取
        response.setHeader("Authorization", "Bearer " + accessJwt);

        Cookie cookie = new Cookie("Refresh-token", refreshJwt);
        cookie.setMaxAge(30 * 24 * 60 * 60);
        cookie.setPath("/api");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        // G18: 与 AuthManager 保持一致，cookieSecure=true(HTTPS) 时设 SameSite=None；dev HTTP 不设让浏览器用默认 Lax
        if (cookieSecure) {
            cookie.setAttribute("SameSite", "None");
        }
        response.addCookie(cookie);

        return Result.success(accessJwt);
    }

    /**
     * 生成访问JWT（30分钟有效期）
     */
    private String createAccessJwt(String accountId, Long userId, Long roleId, Long currentTimeMillis) {
        Map<String, Object> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new HashMap<>();
        payload.put(JWTPayload.ISSUER, "YiRanCrazy");
        payload.put(JWTPayload.SUBJECT, accountId);
        payload.put("userId", userId);
        payload.put("role", roleId); // 统一字段名为role
        payload.put(JWTPayload.EXPIRES_AT, currentTimeMillis + 1000L * 60 * 30);
        payload.put(JWTPayload.NOT_BEFORE, currentTimeMillis);
        payload.put(JWTPayload.ISSUED_AT, currentTimeMillis);
        payload.put(JWTPayload.JWT_ID, String.valueOf(IdUtil.getSnowflakeNextId()));

        return JWTUtil.createToken(header, payload, accessSecretKey.getBytes());
    }

    /**
     * 生成刷新JWT（30天有效期，包含role信息）
     */
    private String createRefreshJwt(String accountId, Long userId, Long roleId, Long currentTimeMillis) {
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

    /**
     * 获取当前登录用户基础信息（支持管理员/医生/药师）
     * <p>B17: 不再二次解析 JWT，直接读 JwtAuthenticationFilter 已校验并 setAttribute 的 currentAccountId，
     * 避免未验签的 parseToken 被未来 filter 链变更绕过。</p>
     * <p>同时按 account.roleId 分流加载 Admin/Doctor/其他，修复 G09 误删导致的医生/药师 /auth/current 永远 "管理员不存在"。</p>
     * @param currentAccountId JwtAuthenticationFilter 注入的 accountId
     * @return 当前登录用户基础信息
     */
    public Result<AdminResponseSimple> getCurrentAdminBaseInfo(Long currentAccountId) {
        if (currentAccountId == null) {
            return Result.fail("未登录");
        }
        Account account = accountService.getAccountById(currentAccountId);
        if (account == null) {
            return Result.fail("账号不存在");
        }
        Role role = roleService.getRoleById(account.getRoleId());
        if (role == null) {
            return Result.fail("账号角色异常");
        }

        AdminResponseSimple result = new AdminResponseSimple();
        result.setId(String.valueOf(account.getUserId()));
        result.setPhone(account.getPhone());
        result.setEmail(account.getEmail());
        result.setRole(role.getName());

        // 按 role 加载对应档案表，取 name/avatar
        long roleId = account.getRoleId() == null ? 0L : account.getRoleId();
        if (roleId == adminRole.getId()) {
            // 管理员
            Admin admin = adminService.getAdminById(account.getUserId());
            if (admin == null) {
                return Result.fail("管理员档案不存在");
            }
            result.setUsername(admin.getName());
            result.setNickname(admin.getName());
            result.setAvatar(admin.getAvatar());
        } else if (roleId == 2L) {
            // 医生：account.userId 约定等于 doctor.id
            Doctor doctor = doctorService.getDoctorById(account.getUserId());
            if (doctor == null) {
                return Result.fail("医生档案不存在");
            }
            result.setUsername(doctor.getName());
            result.setNickname(doctor.getName());
            result.setAvatar(doctor.getAvatar());
        } else {
            // 药师(6)等其他角色无独立档案表，回退用账号手机号
            result.setUsername(account.getPhone());
            result.setNickname(account.getPhone());
            result.setAvatar(null);
        }
        return Result.success(result);
    }
}