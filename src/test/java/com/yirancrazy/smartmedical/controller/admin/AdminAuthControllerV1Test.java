package com.yirancrazy.smartmedical.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirancrazy.smartmedical.manager.AdminAuthManager;
import com.yirancrazy.smartmedical.manager.AuthManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.utils.CaptchaSupport;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * AdminAuthControllerV1 单测
 * 覆盖 BUG-B06: refresh 接口未携带 Refresh-token Cookie 时不抛 500，而是返回业务错误
 */
@WebMvcTest(AdminAuthControllerV1.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAuthControllerV1Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdminAuthManager adminAuthManager;

    @MockitoBean
    private AuthManager userAuthManager;

    @MockitoBean
    private RedisUtil redisUtil;

    // CaptchaVerifyFilter 为 @Component Filter，WebMvcTest 切片会注册它但不会扫描 CaptchaSupport，需显式 mock
    @MockitoBean
    private CaptchaSupport captchaSupport;

    @Test
    void refresh_withoutCookie_shouldReturnFailMessage() throws Exception {
        when(userAuthManager.refresh(isNull(), any(HttpServletResponse.class)))
                .thenReturn(Result.fail("Refresh token 缺失"));

        MvcResult mvcResult = mockMvc.perform(post("/api/admin/v1/auth/refresh"))
                .andExpect(status().isOk())
                .andReturn();

        Result<?> result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                Result.class);

        assertEquals(500, result.getCode());
        assertEquals("Refresh token 缺失", result.getMessage());
    }
}
