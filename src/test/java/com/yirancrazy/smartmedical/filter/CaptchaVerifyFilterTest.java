package com.yirancrazy.smartmedical.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.utils.CaptchaSupport;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 登录滑块守卫过滤器测试
 * @Author: YiRanCrazy@gmail.com
 * @Description: 验证 dev 免校验及 prod 强制校验时守卫接口的放行、拦截行为
 * @Datetime: 2026-09-15 10:35
 * @Version: 1.0
 */

class CaptchaVerifyFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CaptchaSupport captchaSupport;
    private CaptchaVerifyFilter captchaVerifyFilter;

    @BeforeEach
    void setUp() {
        captchaSupport = mock(CaptchaSupport.class);
        captchaVerifyFilter = new CaptchaVerifyFilter(captchaSupport);
        ReflectionTestUtils.setField(captchaVerifyFilter, "loginCaptchaRequired", true);
    }

    /**
     * 关闭登录滑块校验后，三个后台角色的登录接口直接放行
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "/api/admin/v1/auth/login",
            "/api/doctor/v1/auth/login",
            "/api/pharmacy/v1/auth/login"
    })
    void loginCaptchaDisabled_shouldPassGuardedLogin(String uri) throws ServletException, IOException {
        ReflectionTestUtils.setField(captchaVerifyFilter, "loginCaptchaRequired", false);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        captchaVerifyFilter.doFilterInternal(request, response, (req, res) -> chainInvoked.set(true));

        assertTrue(chainInvoked.get());
        verifyNoInteractions(captchaSupport);
    }

    /**
     * 登录滑块校验开启且未通过时，登录接口返回统一业务错误
     */
    @Test
    void loginCaptchaRequiredAndNotPassed_shouldReject() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/admin/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        when(captchaSupport.isPassed(any())).thenReturn(false);

        captchaVerifyFilter.doFilterInternal(request, response, (req, res) -> chainInvoked.set(true));

        Result<?> result = objectMapper.readValue(response.getContentAsString(), Result.class);
        assertEquals(10003, result.getCode());
        assertEquals("请先通过滑块验证", result.getMessage());
        assertFalse(chainInvoked.get());
    }

    /**
     * 登录滑块校验开启且设备已通过时，登录接口正常放行
     */
    @Test
    void loginCaptchaRequiredAndPassed_shouldPass() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/admin/v1/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);
        when(captchaSupport.isPassed(any())).thenReturn(true);

        captchaVerifyFilter.doFilterInternal(request, response, (req, res) -> chainInvoked.set(true));

        assertTrue(chainInvoked.get());
    }
}
