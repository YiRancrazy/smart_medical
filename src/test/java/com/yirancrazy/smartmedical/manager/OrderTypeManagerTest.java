package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.OrderType;
import com.yirancrazy.smartmedical.service.OrderTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * OrderTypeManager 单测
 */
@ExtendWith(MockitoExtension.class)
class OrderTypeManagerTest {

    @Mock private OrderTypeService orderTypeService;

    @InjectMocks
    private OrderTypeManager orderTypeManager;

    @Test
    void addOrderType_setsSnowflakeId() {
        when(orderTypeService.insertOrderType(any(OrderType.class))).thenReturn(1);
        OrderType ot = new OrderType();

        int rows = orderTypeManager.addOrderType(ot);

        assertEquals(1, rows);
        assertNotNull(ot.getId());
    }

    @Test
    void getOrderTypeById_delegates() {
        OrderType ot = new OrderType();
        ot.setId(1L);
        when(orderTypeService.getOrderTypeById(1L)).thenReturn(ot);

        OrderType result = orderTypeManager.getOrderTypeById(1L);

        assertEquals(1L, result.getId());
    }
}
