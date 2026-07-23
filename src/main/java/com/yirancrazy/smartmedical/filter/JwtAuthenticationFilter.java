package com.yirancrazy.smartmedical.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * JWT 认证过滤器
 * @Author: YiRanCrazy@gmail.com
 * @Description: 从 Authorization 头读取 Bearer token → 校验 JWT 签名 + Redis 中是否仍存在；
 *              通过后将主体写入 SecurityContext；失败统一返回 401 JSON。
 * @Datetime: 2026-02-02 12:50
 * @Version: 1.0
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.accessSecretKey}")
    private String accessSecretKey;

    @Value("${jwt.admin.adminAccessTokenPrefix:admin-access-token}")
    private String adminAccessTokenPrefix;

    private final RedisUtil redisUtil;

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String[] WHITE_LIST = {
            "/api/admin/v1/auth/login",
            "/api/admin/v1/auth/refresh",
            "/api/user/v1/auth/login",
            "/api/user/v1/auth/register",
            "/api/user/v1/auth/refresh",
            "/api/doctor/v1/auth/login",
            "/api/doctor/v1/auth/refresh",
            "/api/pharmacy/v1/auth/login",
            "/api/pharmacy/v1/auth/refresh",
            "/doc.html",
            "/swagger-ui",
            "/swagger-ui.html",
            "/swagger-resources",
            "/v2/api-docs",
            "/v3/api-docs",
            "/webjars",
            "/favicon.ico",
            "/error"
    };

    /**
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (isWhitelisted(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        String token = null;

        // 获取token
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            token = header.substring(BEARER_PREFIX.length()).trim();
        }

        // token为空时，返回401
        if (token == null || token.isEmpty()) {
            unauthorized(response, "缺少 access_token");
            return;
        }


        // 验证token
        try {
            JWT jwt = JWTUtil.parseToken(token);

            // 设置验证密钥
            jwt.setKey(accessSecretKey.getBytes());

            // 验证token签名
            if (!jwt.verify()) {
                unauthorized(response, "无效的 access_token");
                return;
            }

            // 验证token有效期
            JWTPayload payload = jwt.getPayload();
            String sub = String.valueOf(payload.getClaim("sub"));
            long exp = Long.parseLong(String.valueOf(payload.getClaim("exp")));

            // exp 字段已经是毫秒级时间戳，直接比较即可
            if (exp < System.currentTimeMillis()) {
                unauthorized(response, "access_token 过期");
                return;
            }

            // 解析 accountId（JWT sub）与 userId claim；login 时以 accountId 为 Redis key 存 token
            Long accountId;
            Long currentUserId;
            try {
                accountId = Long.parseLong(sub);
                Object userIdClaim = payload.getClaim("userId");
                currentUserId = userIdClaim != null ? Long.parseLong(String.valueOf(userIdClaim)) : accountId;
            } catch (NumberFormatException e) {
                unauthorized(response, "无效的 access_token 身份标识");
                return;
            }

            // 校验 Redis 中 token 仍存在（支持 logout 撤销）：login 写入 adminAccessTokenPrefix+accountId，
            // logout 删除同一 key，Filter 必须比对否则登出后旧 token 在 exp 前仍有效
            Object cached = redisUtil.get(adminAccessTokenPrefix + accountId);
            if (cached == null || !token.equals(cached.toString())) {
                unauthorized(response, "access_token 已失效");
                return;
            }

            // todo 后续自定义一个JwtAuthenticationToken
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(sub, null, resolveAuthorities(payload));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 桥接 JWT 上下文到 controller request attributes,
            // 让 controller 可用 @RequestAttribute("currentUserId"/"currentAccountId"/"currentDoctorId"/"currentPharmacistId") 读取已认证身份,
            // 避免 caller-supplied @RequestParam 伪造他人身份。URL 级 role 守卫已由 SecurityConfig 配置。
            request.setAttribute("currentUserId", currentUserId);
            request.setAttribute("currentAccountId", accountId);
            // ponytail: currentDoctorId/currentPharmacistId 暂用 currentUserId，doctor/pharmacist 表 id 与 user.id 的映射在 B10/越权专项修复时补
            request.setAttribute("currentDoctorId", currentUserId);
            request.setAttribute("currentPharmacistId", currentUserId);
            log.debug("JWT 认证通过：accountId={}, userId={}, uri={}", accountId, currentUserId, uri);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("[jwt] 解析 token 失败: {}", e.getMessage());
            unauthorized(response, "无效的 access_token 格式");
        }
    }

    /**
     * 将 JWT payload 中的 role_id 映射为 Spring Security 角色权限（ROLE_xxx）
     * 角色 ID 映射以 role 表为准：1=系统管理员 / 2=医生 / 4=患者 / 6=药师
     * @param payload JWT payload
     * @return Spring Security 角色权限列表
     */
    private List<SimpleGrantedAuthority> resolveAuthorities(JWTPayload payload) {
        Object roleClaim = payload.getClaim("role");
        if (roleClaim == null) {
            return Collections.emptyList();
        }
        long roleId;
        try {
            roleId = Long.parseLong(String.valueOf(roleClaim));
        } catch (NumberFormatException e) {
            return Collections.emptyList();
        }
        String authority = switch ((int) roleId) {
            case 1 -> "ROLE_admin";
            case 2 -> "ROLE_doctor";
            case 6 -> "ROLE_pharmacist";
            default -> "ROLE_user";
        };
        return List.of(new SimpleGrantedAuthority(authority));
    }

    /**
     * 判断 URI 是否在白名单中
     * @param uri URI
     * @return 是否在白名单中
     */
    private boolean isWhitelisted(String uri) {
        for (String allowPath : WHITE_LIST) {
            if (uri.startsWith(allowPath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析 token key
     * @param uri URI
     * @param userId 用户 ID
     * @return token key
     */
    private String resolveTokenKey(String uri, String userId) {
        // 登录时所有角色都写入 adminAccessTokenPrefix + userId，filter 一致读取
        return adminAccessTokenPrefix + userId;
    }

    /**
     * 返回 401 JSON
     * @param response HttpServletResponse
     * @param message 提示信息
     */
    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(401);
        new ObjectMapper().writeValue(response.getWriter(), Result.fail(401, message));
    }
}