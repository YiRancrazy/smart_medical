package com.yirancrazy.smartmedical.constant;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * OrderTypeEnum 单测
 */
class OrderTypeEnumTest {

    @Test
    void getByOrderTypeSn_returnsMatchingEnum() {
        OrderTypeEnum type = OrderTypeEnum.getByOrderTypeSn("3110236876932865049");
        assertNotNull(type);
        assertEquals(OrderTypeEnum.OUTPATIENT_REGISTRATION, type);
        assertEquals("门诊挂号单", type.getName());
    }

    @Test
    void getByOrderTypeSn_eachKnownValueResolves() {
        for (OrderTypeEnum type : OrderTypeEnum.values()) {
            OrderTypeEnum resolved = OrderTypeEnum.getByOrderTypeSn(type.getOrderTypeSn());
            assertEquals(type, resolved);
            assertNotNull(resolved.getName());
        }
    }

    @Test
    void getByOrderTypeSn_returnsNullForUnknown() {
        assertNull(OrderTypeEnum.getByOrderTypeSn("does-not-exist"));
    }

    @Test
    void getByOrderTypeSn_returnsNullForNull() {
        assertNull(OrderTypeEnum.getByOrderTypeSn(null));
    }
}
