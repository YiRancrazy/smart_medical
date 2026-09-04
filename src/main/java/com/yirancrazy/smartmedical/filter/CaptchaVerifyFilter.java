package com.yirancrazy.smartmedical.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.utils.CaptchaSupport;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 滑块验证码守卫过滤器
 * @Author: YiRanCrazy@gmail.com
 * @Description: 拦截登录/注册等需先过滑块的接口，检查当前「设备ID+IP」是否已通过滑块校验
 *              （CaptchaControllerV1.check 写入的标记）；未通过则直接拒绝并提示前端重新滑块。
 * @Datetime: 2026-09-04 14:20
 * @Version: 1.0
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class CaptchaVerifyFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /**
     * 需先通过滑块校验的接口（管理端 / 医生端 / 药师端登录；用户端已取消滑块）
     */
    private static final List<String> GUARDED_PATHS = List.of(
            "/api/admin/v1/auth/login",
            "/api/doctor/v1/auth/login",
            "/api/pharmacy/v1/auth/login"
    );

    private final CaptchaSupport captchaSupport;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!isGuarded(uri)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (captchaSupport.isPassed(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        log.info("[captcha] 未通过滑块校验，拦截 uri={} ip={} device={}",
                uri, captchaSupport.clientIp(request), captchaSupport.deviceId(request));
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpServletResponse.SC_OK);
        new ObjectMapper().writeValue(response.getWriter(), Result.fail(10003, "请先通过滑块验证"));
    }

    private boolean isGuarded(String uri) {
        for (String pattern : GUARDED_PATHS) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }
}