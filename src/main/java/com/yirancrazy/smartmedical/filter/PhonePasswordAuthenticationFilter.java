//package com.yirancrazy.smartmedical.filter;
//
//import org.springframework.http.HttpMethod;
//import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
//import org.springframework.security.web.util.matcher.RequestMatcher;
//
///**
// * @Author: YiRanCrazy@gmail.com
// * @Description: 根据手机号和密码进行认证
// * @Datetime: 2026-02-04 11:22
// * @Version: 1.0
// */
//
//public class PhonePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
//
//    public static String SPRING_SECURITY_FORM_PHONE_KEY = "phone";
//
//    public static String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";
//
//    private static final RequestMatcher DEFAULT_ANT_PATH_REQUEST_MATCHER = PathPatternRequestMatcher.withDefaults()
//            .matcher(HttpMethod.POST, "/login");
//
//    private String phoneParameter = SPRING_SECURITY_FORM_PHONE_KEY;
//
//    private String passwordParameter = SPRING_SECURITY_FORM_PASSWORD_KEY;
//
//    private boolean postOnly = true;
//
//    public PhonePasswordAuthenticationFilter() {
//        super(DEFAULT_ANT_PATH_REQUEST_MATCHER);
//    }
//
//    public PhonePasswordAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
//        super(requiresAuthenticationRequestMatcher);
//    }
//
//
//
//}