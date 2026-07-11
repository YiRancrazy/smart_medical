package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.RoleConstant;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Admin;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.Role;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminResponseSimple;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.AdminService;
import com.yirancrazy.smartmedical.service.RoleService;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCrypt;

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
    private final RedisUtil redisUtil;

    private final Role ADMIN_ROLE = RoleConstant.ROLE_LIST
            .stream()
            .filter(role -> "管理员".equals(role.getName()))
            .findFirst()
            .orElse(null);

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

        List<Account> accountByPhone = accountService.getAccountByPhone(phone);

        // 查找出管理员的账号
        Account adminAccount = accountByPhone
                .stream()
                .filter(account ->  account.getRoleId().equals(ADMIN_ROLE.getId()))
                .findFirst()
                .orElse(null);

        if(adminAccount==null){
            return Result.fail("账号不存在");
        }
        if(!BCrypt.checkpw(password,adminAccount.getPassword())){
            return Result.fail("用户名或密码错误");
        }


        // accessJWT 和 refreshJwt 写入 redis 中
        String accessJwt = createAccessJwt(adminAccount.getPhone(), adminAccount.getId().toString(), adminAccount.getRoleId());
        String refreshJwt = createRefreshJwt(adminAccount.getPhone(),adminAccount.getId().toString());
        redisUtil.setEx(adminAccessTokenPrefix + adminAccount.getId(), accessJwt, 7, TimeUnit.DAYS);
        redisUtil.setEx(adminRefreshTokenPrefix + adminAccount.getId(), refreshJwt, 7, TimeUnit.DAYS);

        response.setHeader("Authorization", "Bearer " + accessJwt);

        Cookie cookie = new Cookie("Refresh-token", refreshJwt);
        cookie.setMaxAge(7 * 24 * 60 * 60);
        cookie.setPath("/api");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        response.addCookie(cookie);

        return Result.success("登录成功");
    }

    private  String createAccessJwt(String username, String accountId, Long roleId){
        Map<String,Object> accessJwtHeader = new HashMap<>();
        accessJwtHeader.put("alg","HS256");
        accessJwtHeader.put("typ","JWT");
        Map<String,Object> accessJwtPayload = new HashMap<>();
        accessJwtPayload.put(JWTPayload.ISSUER,username);
        accessJwtPayload.put(JWTPayload.SUBJECT,accountId);
        accessJwtPayload.put("role_id", roleId);
        accessJwtPayload.put(JWTPayload.EXPIRES_AT,System.currentTimeMillis()+1000*60*60*24*7);
        accessJwtPayload.put(JWTPayload.NOT_BEFORE,System.currentTimeMillis());
        accessJwtPayload.put(JWTPayload.ISSUED_AT,System.currentTimeMillis());
        accessJwtPayload.put(JWTPayload.JWT_ID,String.valueOf(IdUtil.getSnowflakeNextId()));
        return JWTUtil.createToken(accessJwtHeader,accessJwtPayload,accessSecretKey.getBytes());
    }

    private  String createRefreshJwt(String username, String accountId){
        Map<String,Object> refreshJwtHeader = new HashMap<>();
        refreshJwtHeader.put("alg","HS256");
        refreshJwtHeader.put("typ","JWT");
        Map<String,Object> refreshJwtPayload = new HashMap<>();
        refreshJwtPayload.put(JWTPayload.EXPIRES_AT,System.currentTimeMillis()+1000*60*60*24*7);
        refreshJwtPayload.put(JWTPayload.NOT_BEFORE,System.currentTimeMillis());
        refreshJwtPayload.put(JWTPayload.ISSUER,username);
        refreshJwtPayload.put(JWTPayload.SUBJECT,accountId);
        refreshJwtPayload.put(JWTPayload.ISSUED_AT,System.currentTimeMillis());
        refreshJwtPayload.put(JWTPayload.JWT_ID,String.valueOf(IdUtil.getSnowflakeNextId()));
        return JWTUtil.createToken(refreshJwtHeader,refreshJwtPayload,refreshSecretKey.getBytes());
    }

    /**
     * 通过 access_token中存储的 accountId 获取当前管理员基础信息
     * @param request 请求
     * @return 管理员基础信息
     */
    public Result<AdminResponseSimple> getCurrentAdminBaseInfo(HttpServletRequest request) {
        String accessToken = request.getHeader("Authorization");   // 获取 access_token
        if(accessToken == null || accessToken.isEmpty()){
            return Result.fail("未登录");
        }
        JWT jwt = JWTUtil.parseToken(accessToken);  // 解析 access_token
        String accountId = jwt.getPayload("sub").toString(); // 获取 accountId
        Account account = accountService.getAccountById(Long.parseLong(accountId)); // 通过 accountId 获取账号信息
        if(account == null){
            return Result.fail("账号不存在");
        }
        Admin admin = adminService.getAdminById(account.getUserId()); // 通过 adminId 获取管理员信息
        if(admin == null){
            return Result.fail("管理员不存在");
        }
        AdminResponseSimple result = new AdminResponseSimple(); // 创建返回结果
        result.setId(String.valueOf(admin.getId()));
        result.setUsername(admin.getName());
        result.setNickname(admin.getName());
        result.setAvatar(admin.getAvatar());
        result.setPhone(account.getPhone());
        result.setEmail(account.getEmail());
        result.setRole(ADMIN_ROLE.getName()); // 获取角色名称
        return Result.success(result);
    }
}