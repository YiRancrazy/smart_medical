package com.yirancrazy.smartmedical.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 全局配置：将 Long 序列化为 String
 * <p>F02: 雪花 ID（约 1.9e18）超过 JS Number.MAX_SAFE_INTEGER（9e15），
 * 后端以 JSON number 返回时浏览器 JSON.parse 会截断末位，导致前端读到的 ID 与后端不一致。
 * 全局将 Long/long 序列化为 String，前端 ID 类型统一 string，避免精度丢失。</p>
 *
 * @Author: YiRanCrazy@gmail.com
 * @Description: Long → String 全局序列化
 * @Datetime: 2026-07-24 14:00
 * @Version: 1.0
 */
@Configuration
public class JacksonConfig {

    /**
     * 自定义 Jackson2ObjectMapperBuilder：注册 Long → String 的序列化器
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(Long.class, ToStringSerializer.instance);
            module.addSerializer(Long.TYPE, ToStringSerializer.instance);
            builder.modulesToInstall(module);
        };
    }
}
