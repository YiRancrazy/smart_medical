package com.yirancrazy.smartmedical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置
 * @Author: YiRanCrazy@gmail.com
 * @Description: 注册自动填充处理器（create_time/update_time/deleted）
 * @Datetime: 2026-02-02 19:11
 * @Version: 1.0
 */

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusMetaObjectHandler myMetaObjectHandler() {
        return new MybatisPlusMetaObjectHandler();
    }
}