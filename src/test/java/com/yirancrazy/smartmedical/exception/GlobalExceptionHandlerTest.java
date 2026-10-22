package com.yirancrazy.smartmedical.exception;

import com.yirancrazy.smartmedical.pojo.Result;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 全局异常处理测试
 * @Author: YiRanCrazy@gmail.com
 * @Description: 校验 HTTP 方法不匹配时返回 405，而不是笼统的 500
 * @Datetime: 2026-10-22 18:51
 * @Version: 1.0
 */

class GlobalExceptionHandlerTest {

    /**
     * GET 请求命中仅支持 POST 的接口时，应返回 405
     */
    @Test
    void handleMethodNotSupported_shouldReturn405() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/user/v1/auth/logout");

        Result<Void> result = handler.handleMethodNotSupported(
                new HttpRequestMethodNotSupportedException("GET"), request);

        assertEquals(405, result.getCode());
        assertEquals("请求方法不支持", result.getMessage());
    }
}
