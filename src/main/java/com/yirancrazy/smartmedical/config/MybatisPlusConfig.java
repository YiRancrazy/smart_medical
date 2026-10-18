package com.yirancrazy.smartmedical.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
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

    /**
     * 注册 MyBatis-Plus 插件：乐观锁等
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 处方表使用 @Version 乐观锁，必须注册此拦截器
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}