//package com.yirancrazy.smartmedical.filter;
//
//import cn.hutool.jwt.JWTPayload;
//import cn.hutool.jwt.JWTUtil;
//import com.yirancrazy.smartmedical.utils.RedisUtil;
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.NonNull;
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.util.ArrayList;
//
///**
// * @Author: YiRanCrazy@gmail.com
// * @Description:
// * @Datetime: 2026-02-02 12:50
// * @Version: 1.0
// */
//
//@Component
//@RequiredArgsConstructor
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//    @Value("${jwt.accessSecretKey}")
//    private String accessSecretKey;
//
//    private final RedisUtil redisUtil;
//
//    private static final String[] WHITE_LIST = {
//            "/api/v1/auth/login",
//            "/api/v1/auth/register",
//            "/doc.html",
//            "/swagger-ui",
//            "/swagger-resources",
//            "/v2/api-docs",
//            "/v3/api-docs",
//            "/webjars",
//            "/favicon.ico",
//            "/error",
//            "/*","/v3/api-docs", "/swagger-ui", "/swagger-resources", "/swagger-ui.html", "/webjars"
//    };
//
//    @Override
//    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
//        System.out.println(" =============Authorization Testing Start =============");
//        String uri = request.getRequestURI();
//        System.out.println(uri);
//
//        for (String allowPath : WHITE_LIST) {
//            if (uri.startsWith(allowPath)) {
//                System.out.println("[JwtFilter] 白名单放行: " + uri);
//                filterChain.doFilter(request, response);
//                return;
//            }
//        }
//
//        System.out.println("[JwtFilter] 非白名单路径，需要JWT认证: " + uri);
//
//        // 从header 中取出access_token
//        System.out.println(request.getHeader("Authorization"));
//        String requestAccessToken = request.getHeader("access_token");
//        System.out.println("[JwtFilter] token: " + requestAccessToken);
//
//        // 判断token是否存在
//        if (requestAccessToken == null || requestAccessToken.isEmpty()) {
//            System.out.println("[JwtFilter] Token为空，返回401");
//            response.setContentType("application/json; charset=utf-8");
//            response.setStatus(401);
//            response.getWriter().print("{\"success\":false,\"msg\":\"Token is required\"}");
//            return;
//        }
//
//        // 判断是否是有效的jwt
//        if (!JWTUtil.verify(requestAccessToken, accessSecretKey.getBytes())) {
//            System.out.println("[JwtFilter] Token验证失败，返回401");
//            response.setContentType("application/json; charset=utf-8");
//            response.setStatus(401);
//            response.getWriter().print("{\"success\":false,\"msg\":\"Invalid token\"}");
//            return;
//        }
//
//        JWTPayload requestAccessTokenPayload = JWTUtil.parseToken(requestAccessToken).getPayload();
//        // 解析userId
//        String userId = (String) requestAccessTokenPayload.getClaim("sub");
//        // 解析出access_token 中的 exp
//        Long requestAccessTokenExp = Long.parseLong(requestAccessTokenPayload.getClaim("exp").toString());
//
//        System.out.println("[JwtFilter] userId: " + userId + ", exp: " + requestAccessTokenExp);
//
//        boolean allowAccess = false;
//        if (requestAccessTokenExp > System.currentTimeMillis()) {
//            String redisToken = redisUtil.get("access_token_" + userId);
//            // 判断与redis中存储的 access 是否一致
//            if (requestAccessToken.equals(redisToken)) {
//                allowAccess = true;
//            }
//        }
//
//        if (!allowAccess) {
//            System.out.println("[JwtFilter] Token已过期或Redis中不存在，返回401");
//            response.setContentType("application/json; charset=utf-8");
//            response.setStatus(401);
//            response.getWriter().print("{\"success\":false,\"msg\":\"Token expired or invalid\"}");
//            return;
//        }
//
//        System.out.println("[JwtFilter] 认证通过，设置SecurityContext并放行请求");
//
////        // 将认证信息设置到Spring Security的SecurityContext中
////        UsernamePasswordAuthenticationToken authentication =
////                new UsernamePasswordAuthenticationToken("userId", null, new ArrayList<>());
////        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        System.out.println(" =============Authorization Testing End =============");
//        filterChain.doFilter(request, response);
//    }
//}