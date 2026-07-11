package com.yirancrazy.smartmedical.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

/**
 * 手机号 + 密码认证过滤器
 * @Author: YiRanCrazy@gmail.com
 * @Description: 复刻 UsernamePasswordAuthenticationFilter 的表单登录流程，但用 phone/password 作为字段名；
 *              实际项目里 login 走 AuthManager/AdminAuthManager 自行校验，本过滤器用于在
 *              Spring Security 链中对 /login 路径显式做参数提取，便于日志/审计。
 * @Datetime: 2026-02-04 11:22
 * @Version: 1.0
 */

public class PhonePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    public static final String SPRING_SECURITY_FORM_PHONE_KEY = "phone";

    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";

    private static final RequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = PathPatternRequestMatcher.withDefaults()
            .matcher(HttpMethod.POST, "/api/*/v1/auth/login");

    private String phoneParameter = SPRING_SECURITY_FORM_PHONE_KEY;

    private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;

    private boolean postOnly = true;

    public PhonePasswordAuthenticationFilter() {
        super();
        setRequiresAuthenticationRequestMatcher(DEFAULT_ANT_PATH_REQUEST_MATCHER);
    }

    public PhonePasswordAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super();
        setRequiresAuthenticationRequestMatcher(
                requiresAuthenticationRequestMatcher != null
                        ? requiresAuthenticationRequestMatcher
                        : DEFAULT_ANT_PATH_REQUEST_MATCHER);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (postOnly && !HttpMethod.POST.matches(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        String phone = obtainPhone(request);
        phone = (phone != null) ? phone.trim() : "";
        String password = obtainPassword(request);
        password = (password != null) ? password : "";
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(phone, password);
        setDetails(request, authRequest);
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    protected String obtainPhone(HttpServletRequest request) {
        return request.getParameter(this.phoneParameter);
    }

    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(this.passwordParameter);
    }

    public void setPhoneParameter(String phoneParameter) {
        this.phoneParameter = phoneParameter;
    }

    public void setPasswordParameter(String passwordParameter) {
        this.passwordParameter = passwordParameter;
    }

    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }
}