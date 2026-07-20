package com.yirancrazy.smartmedical.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: MyBatis-Plus 配置注册
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