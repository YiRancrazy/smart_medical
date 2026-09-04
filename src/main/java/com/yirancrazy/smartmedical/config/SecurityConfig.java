package com.yirancrazy.smartmedical.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirancrazy.smartmedical.filter.CaptchaVerifyFilter;
import com.yirancrazy.smartmedical.filter.JwtAuthenticationFilter;
import com.yirancrazy.smartmedical.pojo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 无状态 API；JWT 过滤器在 UsernamePasswordAuthenticationFilter 之前执行；
 *              登录端点放行，其余接口统一鉴权。
 * @Datetime: 2026-02-02 12:47
 * @Version: 1.0
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CaptchaVerifyFilter captchaVerifyFilter;

    /**
     * CROSS-04: 统一白名单，供 JwtAuthenticationFilter 与 SecurityFilterChain 共用
     */
    public static final String[] PERMIT_ALL_PATHS = {
            "/api/admin/v1/auth/login",
            "/api/admin/v1/auth/refresh",
            "/api/user/v1/auth/login",
            "/api/user/v1/auth/register",
            "/api/user/v1/auth/refresh",
            "/api/doctor/v1/auth/login",
            "/api/doctor/v1/auth/refresh",
            "/api/pharmacy/v1/auth/login",
            "/api/pharmacy/v1/auth/refresh",
            "/api/captcha/**",
            "/doc.html",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/favicon.ico",
            "/error"
    };

    /**
     * 允许的 CORS 来源；默认 "*" 全放开（dev），生产环境通过 CORS_ALLOWED_ORIGINS 环境变量收紧
     * 多个用逗号分隔，例如：https://admin.hospital.com,https://user.hospital.com
     */
    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    /**
     * 配置安全过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PERMIT_ALL_PATHS).permitAll()
                        .requestMatchers("/api/admin/v1/**").hasRole("admin")
                        .requestMatchers("/api/doctor/v1/**").hasRole("doctor")
                        .requestMatchers("/api/pharmacy/v1/**").hasRole("pharmacist")
                        .requestMatchers("/api/user/v1/**").hasRole("user")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(captchaVerifyFilter, JwtAuthenticationFilter.class)
                .exceptionHandling(exception -> {
                    ObjectMapper mapper = new ObjectMapper();
                    exception.authenticationEntryPoint((request, response, authException) -> {
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                        response.setStatus(401);
                        mapper.writeValue(response.getWriter(), Result.fail(401, "Unauthorized"));
                    });
                    exception.accessDeniedHandler((request, response, accessDeniedException) -> {
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                        response.setStatus(403);
                        mapper.writeValue(response.getWriter(), Result.fail(403, "Access Denied"));
                    });
                });
        return http.build();
    }

    /**
     * CORS 配置：dev 默认 "*" 全放开；prod 通过 cors.allowed-origins 收紧到具体域名
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // G19: Allow-Credentials=true 时不能返回 Access-Control-Allow-Origin: *，
        // 使用 originPatterns 支持通配并允许携带 Cookie
        configuration.setAllowedOriginPatterns(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * CROSS-01: prod 环境启动时校验 CORS 来源，禁止通配符与 Allow-Credentials=true 共存
     */
    @Configuration
    @Profile("prod")
    public static class ProdCorsOriginChecker {
        @Value("${cors.allowed-origins:*}")
        private String allowedOrigins;

        @PostConstruct
        public void check() {
            if (allowedOrigins == null || allowedOrigins.trim().isEmpty() || allowedOrigins.contains("*")) {
                throw new IllegalStateException("生产环境 CORS 来源不能为通配符，请配置 cors.allowed-origins 为具体域名");
            }
        }
    }

    /**
     * 密码编码器（BCrypt）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}