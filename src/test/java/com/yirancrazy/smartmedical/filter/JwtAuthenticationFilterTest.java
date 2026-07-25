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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * JwtAuthenticationFilter 单测
 * 覆盖 BUG-B10: Redis 不可达时仍允许有效 token 通过
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

    private JwtAuthenticationFilter filter;

    private final String accessSecretKey = "test-secret-key-32bytes-long!!!!!";

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(redisUtil);
        ReflectionTestUtils.setField(filter, "accessSecretKey", accessSecretKey);
        ReflectionTestUtils.setField(filter, "adminAccessTokenPrefix", "admin-access-token");
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_whenRedisUnavailable_shouldStillPass() throws Exception {
        Long accountId = 123456789L;
        String token = JWT.create()
                .setPayload("sub", accountId.toString())
                .setPayload("userId", accountId.toString())
                .setPayload("role", "1")
                .setPayload("exp", System.currentTimeMillis() + 3600000)
                .setKey(accessSecretKey.getBytes())
                .sign();

        when(request.getRequestURI()).thenReturn("/api/admin/v1/user/profile");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(redisUtil.get(anyString())).thenThrow(new RuntimeException("Redis connection timeout"));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }
}
