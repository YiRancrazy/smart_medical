package com.yirancrazy.smartmedical.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 跨域配置类
 * @DateTime: 2024/4/10 12:01
 * @Version: 1.0
 **/

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的前端地址
                .allowedOrigins("http://localhost:5173", "http://localhost:5174")
                // 允许的请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 暴露 Authorization 响应头给前端
                .exposedHeaders("Authorization")
                // 允许携带凭证（cookies等）
                .allowCredentials(true)
                // 预检请求缓存时间
                .maxAge(3600);
    }
}