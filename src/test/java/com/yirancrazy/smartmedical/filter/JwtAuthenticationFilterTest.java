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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    void doFilterInternal_doctorRoleAsInteger_shouldSetCurrentDoctorId() throws Exception {
        Long accountId = 123456789L;
        String token = buildToken(accountId, 2);

        when(request.getRequestURI()).thenReturn("/api/doctor/v1/prescription/list");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(redisUtil.get(anyString())).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("currentDoctorId", accountId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_doctorRoleAsLong_shouldSetCurrentDoctorId() throws Exception {
        // 登录时 roleId 以 Long 存入 payload（见 AdminAuthManager.createAccessJwt），需兼容
        Long accountId = 123456789L;
        String token = buildToken(accountId, 2L);

        when(request.getRequestURI()).thenReturn("/api/doctor/v1/prescription/list");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(redisUtil.get(anyString())).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("currentDoctorId", accountId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_doctorRoleAsString_shouldSetCurrentDoctorId() throws Exception {
        // 兼容历史 token（claim 存字符串）
        Long accountId = 123456789L;
        String token = buildToken(accountId, "2");

        when(request.getRequestURI()).thenReturn("/api/doctor/v1/prescription/list");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(redisUtil.get(anyString())).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        verify(request).setAttribute("currentDoctorId", accountId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_patientRole_shouldNotSetCurrentDoctorId() throws Exception {
        Long accountId = 123456789L;
        String token = buildToken(accountId, 4);

        when(request.getRequestURI()).thenReturn("/api/user/v1/registration/list");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(redisUtil.get(anyString())).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        verify(request, never()).setAttribute(eq("currentDoctorId"), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_missingRoleClaim_shouldNotSetCurrentDoctorId() throws Exception {
        Long accountId = 123456789L;
        String token = buildToken(accountId, null);

        when(request.getRequestURI()).thenReturn("/api/admin/v1/user/profile");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(redisUtil.get(anyString())).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        verify(request, never()).setAttribute(eq("currentDoctorId"), any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidRoleClaim_shouldNotSetCurrentDoctorId() throws Exception {
        Long accountId = 123456789L;
        String token = buildToken(accountId, "abc");

        when(request.getRequestURI()).thenReturn("/api/admin/v1/user/profile");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(redisUtil.get(anyString())).thenReturn(token);

        filter.doFilterInternal(request, response, filterChain);

        verify(request, never()).setAttribute(eq("currentDoctorId"), any());
        verify(filterChain).doFilter(request, response);
    }

    /**
     * 构造带指定 role claim 的签名 JWT（sub / userId 相同；role 为 null 时不写入 claim）
     */
    private String buildToken(Long accountId, Object role) {
        JWT jwt = JWT.create()
                .setPayload("sub", accountId.toString())
                .setPayload("userId", accountId.toString())
                .setPayload("exp", System.currentTimeMillis() / 1000 + 3600)
                .setKey(accessSecretKey.getBytes());
        if (role != null) {
            jwt.setPayload("role", role);
        }
        return jwt.sign();
    }
}
