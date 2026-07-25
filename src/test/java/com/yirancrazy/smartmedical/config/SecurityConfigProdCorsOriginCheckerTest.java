package com.yirancrazy.smartmedical.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * SecurityConfig 单测
 * 覆盖 CROSS-01: prod 环境 CORS 来源禁止通配符
 */
class SecurityConfigProdCorsOriginCheckerTest {

    @Test
    void check_whenWildcardOrigin_shouldThrow() {
        SecurityConfig.ProdCorsOriginChecker checker = new SecurityConfig.ProdCorsOriginChecker();
        ReflectionTestUtils.setField(checker, "allowedOrigins", "*");
        assertThrows(IllegalStateException.class, checker::check);
    }

    @Test
    void check_whenConcreteOrigin_shouldPass() {
        SecurityConfig.ProdCorsOriginChecker checker = new SecurityConfig.ProdCorsOriginChecker();
        ReflectionTestUtils.setField(checker, "allowedOrigins", "https://admin.hospital.com");
        assertDoesNotThrow(checker::check);
    }
}
