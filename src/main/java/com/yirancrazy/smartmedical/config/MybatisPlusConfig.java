package com.yirancrazy.smartmedical.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: YiRanCrazy@gmail.com
<<<<<<< HEAD
 * @Description: MyBatis-Plus 配置注册
=======
 * @Description: 注册自动填充处理器（create_time/update_time/deleted）与乐观锁拦截器
>>>>>>> fix/prescription-lock-idempotency
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
<<<<<<< HEAD
     * 注册 MyBatis-Plus 插件：乐观锁等
=======
     * 注册乐观锁拦截器，使实体 @Version 字段真正参与 UPDATE 条件
>>>>>>> fix/prescription-lock-idempotency
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
<<<<<<< HEAD
        // 处方表使用 @Version 乐观锁，必须注册此拦截器
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
=======
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }
}
>>>>>>> fix/prescription-lock-idempotency
