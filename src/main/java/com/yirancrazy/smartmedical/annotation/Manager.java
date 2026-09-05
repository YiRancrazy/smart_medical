package com.yirancrazy.smartmedical.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: Manager 层注解
 * @Datetime: 2026-02-02 13:14
 * @Version: 1.0
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Manager {
    @AliasFor(
            annotation = Component.class
    )
    String value() default "";
}
