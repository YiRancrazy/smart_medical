package com.yirancrazy.smartmedical.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yirancrazy.smartmedical.filter.JwtAuthenticationFilter;
import com.yirancrazy.smartmedical.pojo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                        .requestMatchers(
                                "/api/admin/v1/auth/login",
                                "/api/admin/v1/auth/refresh",
                                "/api/user/v1/auth/login",
                                "/api/user/v1/auth/register",
                                "/api/user/v1/auth/refresh",
                                "/api/doctor/v1/auth/login",
                                "/api/doctor/v1/auth/refresh",
                                "/api/pharmacy/v1/auth/login",
                                "/api/pharmacy/v1/auth/refresh",
                                "/doc.html",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/error"
                        ).permitAll()
                        .requestMatchers("/api/admin/v1/**").hasRole("admin")
                        .requestMatchers("/api/doctor/v1/**").hasRole("doctor")
                        .requestMatchers("/api/pharmacy/v1/**").hasRole("pharmacist")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
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
     * CORS 配置（开发环境放开；生产建议收紧来源）
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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