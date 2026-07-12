package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.service.OrderItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderItemManager 单测
 */
@ExtendWith(MockitoExtension.class)
class OrderItemManagerTest {

    @Mock private OrderItemService orderItemService;

    @InjectMocks
    private OrderItemManager orderItemManager;

    @Test
    void addOrderItem_setsSnowflakeId() {
        when(orderItemService.insertOrderItem(any(OrderItem.class))).thenReturn(1);

        int rows = orderItemManager.addOrderItem(new OrderItem());

        assertEquals(1, rows);
        verify(orderItemService).insertOrderItem(any(OrderItem.class));
    }

    @Test
    void addOrderItem_assignsNonNullId() {
        when(orderItemService.insertOrderItem(any(OrderItem.class))).thenReturn(1);
        OrderItem item = new OrderItem();

        orderItemManager.addOrderItem(item);

        assertNotNull(item.getId());
    }

    @Test
    void getOrderItemById_delegates() {
        OrderItem item = new OrderItem();
        item.setId(99L);
        when(orderItemService.getOrderItemById(99L)).thenReturn(item);

        OrderItem result = orderItemManager.getOrderItemById(99L);

        assertEquals(99L, result.getId());
    }
}
