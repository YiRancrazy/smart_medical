package com.yirancrazy.smartmedical.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT Cookie 写入工具
 * <p>
 * 提取自 {@code AuthManager#login} 与 {@code AdminAuthManager#loginByPhoneAndPassword}
 * 中两处 byte-identical 的 Refresh-token Cookie 写入块。
 *
 * @Author: YiRanCrazy@gmail.com
 * @Description: JWT Cookie 写入工具
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */
public final class JwtCookieUtil {

    /** Cookie 名 */
    public static final String REFRESH_COOKIE_NAME = "Refresh-token";
    /** Cookie 路径 */
    public static final String REFRESH_COOKIE_PATH = "/api";
    /** Cookie 有效期（秒）30 天 */
    public static final int REFRESH_COOKIE_MAX_AGE = 30 * 24 * 60 * 60;

    private JwtCookieUtil() {
    }

    /**
     * 写入 Refresh-token Cookie 到响应
     *
     * @param response   HTTP 响应
     * @param refreshJwt Refresh JWT 内容
     */
    public static void writeRefreshCookie(HttpServletResponse response, String refreshJwt) {
        if (response == null) {
            return;
        }
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, refreshJwt);
        cookie.setMaxAge(REFRESH_COOKIE_MAX_AGE);
        cookie.setPath(REFRESH_COOKIE_PATH);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        response.addCookie(cookie);
    }
}