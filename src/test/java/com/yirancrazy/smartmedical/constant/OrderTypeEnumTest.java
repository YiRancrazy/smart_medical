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
    void getByCode_returnsMatchingEnum() {
        OrderTypeEnum type = OrderTypeEnum.getByCode(1L);
        assertNotNull(type);
        assertEquals(OrderTypeEnum.REGISTRATION, type);
        assertEquals("挂号订单", type.getName());
    }

    @Test
    void getByCode_eachKnownValueResolves() {
        for (OrderTypeEnum type : OrderTypeEnum.values()) {
            OrderTypeEnum resolved = OrderTypeEnum.getByCode(type.getCode());
            assertEquals(type, resolved);
            assertNotNull(resolved.getName());
        }
    }

    @Test
    void getByCode_returnsNullForUnknown() {
        assertNull(OrderTypeEnum.getByCode(99L));
    }

    @Test
    void getByCode_returnsNullForNull() {
        assertNull(OrderTypeEnum.getByCode(null));
    }
}
