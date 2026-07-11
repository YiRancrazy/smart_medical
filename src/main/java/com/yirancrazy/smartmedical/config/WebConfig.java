//package com.yirancrazy.smartmedical.config;
//
//import com.yirancrazy.smartmedical.interceptor.LoginInterceptor;
//import jakarta.annotation.Resource;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import lombok.NonNull;
//
///**
// * @Author: YiRanCrazy@gmail.com
// * @Description:
// * @Datetime: 2026-02-02 15:15
// * @Version: 1.0
// */
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Resource
//    private LoginInterceptor loginInterceptor;
//
//    @Override
//    public void addInterceptors(@NonNull InterceptorRegistry registry) {
//        registry.addInterceptor(loginInterceptor).addPathPatterns("/**");
//    }
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//
//        registry.
//                addResourceHandler("/swagger-ui/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
//                .resourceChain(false);
//        registry.addResourceHandler("/swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
//
//
//}