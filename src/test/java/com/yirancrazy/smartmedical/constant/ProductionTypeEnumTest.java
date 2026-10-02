package com.yirancrazy.smartmedical.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: ProductionTypeEnum 单测
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */
class ProductionTypeEnumTest {

    @Test
    void getByCode_returnsMatchingEnum() {
        ProductionTypeEnum type = ProductionTypeEnum.getByCode(1L);
        assertNotNull(type);
        assertEquals(ProductionTypeEnum.DRUG, type);
        assertEquals("药品", type.getName());
    }

    @Test
    void registrationTypeIsIndependentFromDrugType() {
        assertEquals(6L, ProductionTypeEnum.REGISTRATION.getCode());
        assertEquals("挂号", ProductionTypeEnum.REGISTRATION.getName());
    }

    @Test
    void getByCode_returnsNullForUnknown() {
        assertNull(ProductionTypeEnum.getByCode(99L));
        assertNull(ProductionTypeEnum.getByCode(null));
    }
}
