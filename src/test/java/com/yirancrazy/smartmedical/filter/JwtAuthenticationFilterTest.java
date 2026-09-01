package com.yirancrazy.smartmedical.filter;

import cn.hutool.jwt.JWT;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.PrintWriter;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JwtAuthenticationFilter 单测
 * 覆盖 BUG-B10: Redis 不可达时仍允许有效 token 通过
 * 覆盖 BUG-B01: 白名单精确匹配，避免 startsWith 前缀绕过
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    private JwtAuthenticationFilter filter;

    private final String accessSecretKey = "test-secret-key-32bytes-long!!!!!";

    @BeforeEach
    void setUp() throws Exception {
        filter = new JwtAuthenticationFilter(redisUtil);
        ReflectionTestUtils.setField(filter, "accessSecretKey", accessSecretKey);
        ReflectionTestUtils.setField(filter, "accessTokenPrefix", "admin-access-token");
        SecurityContextHolder.clearContext();
        lenient().when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void doFilterInternal_whenRedisUnavailable_shouldStillPass() throws Exception {
        Long accountId = 123456789L;
        String token = JWT.create()
                .setPayload("sub", accountId.toString())
                .setPayload("userId", accountId.toString())
                .setPayload("role", "1")
                .setPayload("exp", System.currentTimeMillis() / 1000 + 3600)
                .setKey(accessSecretKey.getBytes())
                .sign();

        when(request.getRequestURI()).thenReturn("/api/admin/v1/user/profile");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(redisUtil.get(anyString())).thenThrow(new RuntimeException("Redis connection timeout"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_whitelistedExactPath_shouldPassWithoutToken() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/admin/v1/auth/login");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_startsWithPrefix_shouldNotBypassWhitelist() throws Exception {
        // 旧代码 startsWith("/swagger-ui") 会错误放行 /swagger-uiXYZ
        when(request.getRequestURI()).thenReturn("/swagger-uiXYZ/api/admin/v1/user/profile");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(401);
    }
}
