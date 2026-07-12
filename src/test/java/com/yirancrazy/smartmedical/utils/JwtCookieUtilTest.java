package com.yirancrazy.smartmedical.utils;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JwtCookieUtil 单测
 */
class JwtCookieUtilTest {

    @Test
    void writeRefreshCookie_setsExpectedAttributes() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        String jwt = "fake.jwt.token";

        JwtCookieUtil.writeRefreshCookie(response, jwt);

        Cookie cookie = response.getCookie(JwtCookieUtil.REFRESH_COOKIE_NAME);
        assertNotNull(cookie, "Refresh-token cookie should be set");
        assertEquals(jwt, cookie.getValue());
        assertEquals(JwtCookieUtil.REFRESH_COOKIE_PATH, cookie.getPath());
        assertEquals(JwtCookieUtil.REFRESH_COOKIE_MAX_AGE, cookie.getMaxAge());
        assertTrue(cookie.isHttpOnly(), "cookie should be HttpOnly");
        assertTrue(cookie.getSecure(), "cookie should be Secure");
    }

    @Test
    void writeRefreshCookie_nullResponseDoesNotThrow() {
        assertDoesNotThrow(() -> JwtCookieUtil.writeRefreshCookie(null, "anything"));
    }

    @Test
    void writeRefreshCookie_acceptsEmptyToken() {
        MockHttpServletResponse response = new MockHttpServletResponse();
        JwtCookieUtil.writeRefreshCookie(response, "");
        Cookie cookie = response.getCookie(JwtCookieUtil.REFRESH_COOKIE_NAME);
        assertNotNull(cookie);
        assertEquals("", cookie.getValue());
    }

    @Test
    void constants_haveExpectedValues() {
        assertEquals("Refresh-token", JwtCookieUtil.REFRESH_COOKIE_NAME);
        assertEquals("/api", JwtCookieUtil.REFRESH_COOKIE_PATH);
        assertEquals(30 * 24 * 60 * 60, JwtCookieUtil.REFRESH_COOKIE_MAX_AGE);
    }
}