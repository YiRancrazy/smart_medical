//package com.yirancrazy.smartmedical.config;
//
//import com.yirancrazy.smartmedical.filter.JwtAuthenticationFilter;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//
///**
// * @Author: YiRanCrazy@gmail.com
// * @Description: SecurityConfig 配置类
// * @Datetime: 2026-02-02 12:47
// * @Version: 1.0
// */
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//public class SecurityConfig {
//
//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//
//
//    /**
//     * 配置安全过滤器链 - Spring Security的核心配置方法
//     * 定义URL访问规则、会话管理、CSRF防护等安全策略[5](@ref)
//     */
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                // 配置CORS跨域支持
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                // 禁用CSRF防护（适用于基于JWT的无状态API）[8](@ref)
//                .csrf(csrf -> csrf.disable())
//                // 设置无状态会话管理（适用于RESTful API）[5](@ref)
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                // 配置请求授权规则
//                .authorizeHttpRequests(auth -> auth
//                        // 允许匿名访问的端点（登录和注册接口）[5](@ref)
//                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/doc.html","/v3/api-docs/**","/favicon.ico", "/swagger-ui/**", "/swagger-resources/**","/swagger-ui.html").permitAll()
//                        .requestMatchers("/api/v1/user", "/api/v1/user/**", "api/v1/user", "api/v1/user/**", "/webjars/**").permitAll()
//                        .requestMatchers("/error").permitAll()
//                        .anyRequest().authenticated()
//                )
//                // 添加JWT认证过滤器到UsernamePasswordAuthenticationFilter之前[5](@ref)
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                // 配置异常处理，输出详细的拦截日志
//                .exceptionHandling(exception -> exception
//                        .authenticationEntryPoint((request, response, authException) -> {
//                            System.out.println("[SecurityConfig] AuthenticationEntryPoint - 请求被拦截: " + request.getRequestURI());
//                            System.out.println("[SecurityConfig] 原因: " + authException.getMessage());
//                            response.setContentType("application/json; charset=utf-8");
//                            response.setStatus(401);
//                            response.getWriter().print("{\"success\":false,\"msg\":\"Unauthorized\"}");
//                        })
//                        .accessDeniedHandler((request, response, accessDeniedException) -> {
//                            System.out.println("[SecurityConfig] AccessDeniedHandler - 请求被拒绝: " + request.getRequestURI());
//                            System.out.println("[SecurityConfig] 原因: " + accessDeniedException.getMessage());
//                            response.setContentType("application/json; charset=utf-8");
//                            response.setStatus(403);
//                            response.getWriter().print("{\"success\":false,\"msg\":\"Access Denied\"}");
//                        })
//                );
//
//
//        return http.build();
//    }
//
//    /**
//     * 配置CORS跨域资源共享
//     * 允许前端应用跨域访问后端API[5](@ref)
//     */
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        // 允许所有来源（生产环境中建议指定具体域名）[5](@ref)
//        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
//        // 允许的HTTP方法
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        // 允许所有请求头
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        // 允许携带认证信息（如Cookie、Authorization头）
//        configuration.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        // 对所有URL路径应用CORS配置
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//
//    /**
//     * 配置密码编码器 - 使用BCrypt强哈希加密[5](@ref)
//     * 用于用户密码的加密和验证
//     */
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    /**
//     * 配置认证管理器Bean
//     * 用于处理用户认证请求[5](@ref)
//     */
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//}