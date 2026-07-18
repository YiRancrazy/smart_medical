package com.yirancrazy.smartmedical.filter;

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
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            token = header.substring(BEARER_PREFIX.length()).trim();
        }
        if (token == null || token.isEmpty()) {
            unauthorized(response, "Token is required");
            return;
        }
        try {
            if (!JWTUtil.verify(token, accessSecretKey.getBytes())) {
                unauthorized(response, "Invalid token");
                return;
            }
            JWTPayload payload = JWTUtil.parseToken(token).getPayload();
            String userId = String.valueOf(payload.getClaim("sub"));
            Long exp = Long.parseLong(String.valueOf(payload.getClaim("exp")));
            if (exp == null || exp < System.currentTimeMillis()) {
                unauthorized(response, "Token expired");
                return;
            }
            String redisToken = redisUtil.get(resolveTokenKey(uri, userId));
            if (redisToken == null || !redisToken.equals(token)) {
                unauthorized(response, "Token expired or invalid");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, resolveAuthorities(payload));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // 桥接 JWT 上下文到 controller request attributes,
            // 让 controller 可用 @RequestAttribute("currentUserId"/"currentDoctorId"/"currentPharmacistId") 读取已认证身份,
            // 避免 caller-supplied @RequestParam 伪造他人身份。URL 级 role 守卫已由 SecurityConfig 配置。
            try {
                Long currentUserId = Long.parseLong(userId);
                request.setAttribute("currentUserId", currentUserId);
                request.setAttribute("currentDoctorId", currentUserId);
                request.setAttribute("currentPharmacistId", currentUserId);
            } catch (NumberFormatException e) {
                log.warn("[jwt] userId 非数字,跳过 request attribute 桥接: {}", userId);
            }
            log.debug("JWT 认证通过：userId={}, uri={}", userId, uri);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("[jwt] 解析 token 失败: {}", e.getMessage());
            unauthorized(response, "Invalid token format");
        }
    }

    /**
     * 将 JWT payload 中的 role_id 映射为 Spring Security 角色权限（ROLE_xxx）
     * 角色 ID 映射以 role 表为准：1=系统管理员 / 2=医生 / 4=患者 / 6=药师
     */
    private List<SimpleGrantedAuthority> resolveAuthorities(JWTPayload payload) {
        Object roleClaim = payload.getClaim("role_id");
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

    private boolean isWhitelisted(String uri) {
        for (String allowPath : WHITE_LIST) {
            if (uri.startsWith(allowPath)) {
                return true;
            }
        }
        return false;
    }

    private String resolveTokenKey(String uri, String userId) {
        // 登录时所有角色都写入 adminAccessTokenPrefix + userId，filter 一致读取
        return adminAccessTokenPrefix + userId;
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(401);
        new ObjectMapper().writeValue(response.getWriter(), Result.fail(401, message));
    }
}