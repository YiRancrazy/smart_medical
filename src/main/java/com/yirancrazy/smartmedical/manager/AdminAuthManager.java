package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.RoleConstant;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Admin;
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

    @Value("${jwt.admin.adminAccessTokenPrefix}")
    private String adminAccessTokenPrefix;   // 固定管理员登录令牌前缀\
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
        if (adminAccessTokenPrefix == null || adminAccessTokenPrefix.isBlank()) {
            throw new IllegalStateException("jwt.admin.adminAccessTokenPrefix 未配置");
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
        if (!PasswordUtil.verify(password, roleAccount.getPassword())) {
            return Result.fail("用户名或密码错误");
        }

        // 历史弱密码（MD5/明文）登录后自动升级为 BCrypt，逐步消除弱密码存储
        if (PasswordUtil.needsBcryptUpgrade(roleAccount.getPassword())) {
            roleAccount.setPassword(PasswordUtil.encode(password));
            accountService.updateAccountById(roleAccount);
            log.info("[admin-login] 密码已升级为 BCrypt, accountId={}", roleAccount.getId());
        }

        // accessJWT 和 refreshJwt 写入 redis 中（admin前缀用于所有角色，统一管理）
        Long currentTimeMillis = System.currentTimeMillis();
        String accessJwt = createAccessJwt(roleAccount.getId().toString(), roleAccount.getUserId(), roleAccount.getRoleId(), currentTimeMillis);
        String refreshJwt = createRefreshJwt(roleAccount.getId().toString(), roleAccount.getUserId(), roleAccount.getRoleId(), currentTimeMillis);
        redisUtil.setEx(adminAccessTokenPrefix + roleAccount.getId(), accessJwt, 30, TimeUnit.MINUTES);
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
     * 通过 access_token 获取当前登录用户基础信息（支持管理员/医生/药师）
     * @param request 请求
     * @return 当前登录用户基础信息
     */
    public Result<AdminResponseSimple> getCurrentAdminBaseInfo(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization");   // 获取 access_token
        if(accessToken == null || accessToken.isEmpty()){
            return Result.fail("未登录");
        }
        // 去掉 "Bearer " 前缀
        if (accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }
        JWT jwt = JWTUtil.parseToken(accessToken);  // 解析 access_token
        String accountId = jwt.getPayload("sub").toString(); // 获取 accountId
        Object roleClaim = jwt.getPayload("role");
        Long roleId = roleClaim == null ? adminRole.getId() : Long.parseLong(String.valueOf(roleClaim));
        Account account = accountService.getAccountById(Long.parseLong(accountId)); // 通过 accountId 获取账号信息
        if(account == null){
            return Result.fail("账号不存在");
        }
        AdminResponseSimple result = new AdminResponseSimple();
        result.setId(String.valueOf(account.getUserId()));
        result.setPhone(account.getPhone());
        result.setEmail(account.getEmail());
        // G09: 接口路径在 /api/admin/v1/** 下，SecurityConfig 仅允许 ROLE_admin 访问，
        //      医生/药师 token 无法调用，原 role=2/6 分支为死代码，已删除
        Admin admin = adminService.getAdminById(account.getUserId()); // 通过 adminId 获取管理员信息
        if(admin == null){
            return Result.fail("管理员不存在");
        }
        result.setUsername(admin.getName());
        result.setNickname(admin.getName());
        result.setAvatar(admin.getAvatar());
        result.setRole(adminRole.getName()); // 获取角色名称
        return Result.success(result);
    }
}