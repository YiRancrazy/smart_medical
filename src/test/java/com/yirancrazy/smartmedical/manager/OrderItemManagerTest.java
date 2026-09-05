package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.service.OrderItemService;
import com.yirancrazy.smartmedical.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderItemManager 单测
 */
@ExtendWith(MockitoExtension.class)
class OrderItemManagerTest {

    @Mock private OrderItemService orderItemService;
    @Mock private OrderService orderService;

    @InjectMocks
    private OrderItemManager orderItemManager;

    @Test
    void addOrderItem_ownedOrder_setsSnowflakeId() {
        OrderItem item = new OrderItem();
        item.setOrderId(1L);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(3001L);
        when(orderService.getOrderById(1L)).thenReturn(order);
        when(orderItemService.insertOrderItem(any(OrderItem.class))).thenReturn(1);

        int rows = orderItemManager.addOrderItem(item, 3001L);

        assertEquals(1, rows);
        assertNotNull(item.getId());
        verify(orderItemService).insertOrderItem(any(OrderItem.class));
    }

    @Test
    void addOrderItem_missingOrderId_throws() {
        BizException ex = assertThrows(BizException.class,
                () -> orderItemManager.addOrderItem(new OrderItem(), 3001L));
        assertEquals(5001, ex.getCode());
    }

    @Test
    void addOrderItem_unownedOrder_throws() {
        OrderItem item = new OrderItem();
        item.setOrderId(1L);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(9999L);
        when(orderService.getOrderById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class,
                () -> orderItemManager.addOrderItem(item, 3001L));
        assertEquals(5002, ex.getCode());
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