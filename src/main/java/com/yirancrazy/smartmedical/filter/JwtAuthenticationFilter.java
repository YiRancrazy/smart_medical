package com.yirancrazy.smartmedical.filter;

import cn.hutool.jwt.JWTPayload;
import cn.hutool.jwt.JWTUtil;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

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

    private final RedisUtil redisUtil;

    private static final String BEARER_PREFIX = "Bearer ";

    private static final String[] WHITE_LIST = {
            "/api/admin/v1/auth/login",
            "/api/user/v1/auth/login",
            "/api/user/v1/auth/register",
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
        String redisToken = redisUtil.get("access_token_" + userId);
        if (redisToken == null || !redisToken.equals(token)) {
            unauthorized(response, "Token expired or invalid");
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("JWT 认证通过：userId={}, uri={}", userId, uri);
        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String uri) {
        for (String allowPath : WHITE_LIST) {
            if (uri.startsWith(allowPath)) {
                return true;
            }
        }
        return false;
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(401);
        response.getWriter().print("{\"success\":false,\"msg\":\"" + message + "\"}");
    }
}