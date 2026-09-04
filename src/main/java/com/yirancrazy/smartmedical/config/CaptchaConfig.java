package com.yirancrazy.smartmedical.config;

import com.anji.captcha.config.AjCaptchaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * AJ-Captcha 行为验证码配置（Spring Boot 3 手动装配）
 * @Author: YiRanCrazy@gmail.com
 * @Description: captcha-spring-boot-starter 1.4.0 通过 META-INF/spring.factories 注册自动配置，
 *              Spring Boot 3 只识别 AutoConfiguration.imports，故未扫描到该配置导致
 *              CaptchaService 依赖缺失、应用启动失败。此处手动 @Import 该自动配置，
 *              以启用 aj.captcha 属性绑定并注册 CaptchaService / CaptchaCacheService Bean。
 * @Datetime: 2026-09-04 16:55
 * @Version: 1.0
 */

@Configuration
@Import(AjCaptchaAutoConfiguration.class)
public class CaptchaConfig {
}