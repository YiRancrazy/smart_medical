package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.service.OrderItemService;
import com.yirancrazy.smartmedical.service.OrderService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderItemManager 单测
 */
@ExtendWith(MockitoExtension.class)
class OrderItemManagerTest {

    @Mock private OrderItemService orderItemService;
    @Mock private OrderService orderService;
    @Mock private UserPatientRelationService userPatientRelationService;

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
        verify(userPatientRelationService, never()).hasPatientRelation(anyLong(), anyLong());
    }

    @Test
    void addOrderItem_relatedPatient_setsSnowflakeId() {
        OrderItem item = new OrderItem();
        item.setOrderId(1L);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(3001L);
        order.setPatientId(8001L);
        when(orderService.getOrderById(1L)).thenReturn(order);
        when(userPatientRelationService.hasPatientRelation(4001L, 8001L)).thenReturn(true);
        when(orderItemService.insertOrderItem(any(OrderItem.class))).thenReturn(1);

        int rows = orderItemManager.addOrderItem(item, 4001L);

        assertEquals(1, rows);
        assertNotNull(item.getId());
        verify(userPatientRelationService).hasPatientRelation(4001L, 8001L);
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
    void addOrderItem_nonOwnerWithNullPatientId_throwsWithoutRelationQuery() {
        OrderItem item = new OrderItem();
        item.setOrderId(1L);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(9999L);
        when(orderService.getOrderById(1L)).thenReturn(order);

        BizException ex = assertThrows(BizException.class,
                () -> orderItemManager.addOrderItem(item, 3001L));

        assertEquals(5002, ex.getCode());
        verify(userPatientRelationService, never()).hasPatientRelation(anyLong(), anyLong());
    }

    @Test
    void getOrderItemById_ownedOrder_returnsItem() {
        OrderItem item = new OrderItem();
        item.setId(99L);
        item.setOrderId(1L);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(3001L);
        when(orderItemService.getOrderItemById(99L)).thenReturn(item);
        when(orderService.getOrderById(1L)).thenReturn(order);

        OrderItem result = orderItemManager.getOrderItemById(99L, 3001L);

        assertEquals(99L, result.getId());
        verify(userPatientRelationService, never()).hasPatientRelation(anyLong(), anyLong());
    }

    @Test
    void getOrderItemById_relatedPatient_returnsItem() {
        OrderItem item = new OrderItem();
        item.setId(99L);
        item.setOrderId(1L);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(3001L);
        order.setPatientId(8001L);
        when(orderItemService.getOrderItemById(99L)).thenReturn(item);
        when(orderService.getOrderById(1L)).thenReturn(order);
        when(userPatientRelationService.hasPatientRelation(4001L, 8001L)).thenReturn(true);

        OrderItem result = orderItemManager.getOrderItemById(99L, 4001L);

        assertEquals(99L, result.getId());
    }

    @Test
    void getOrderItemById_unownedOrder_throws() {
        OrderItem item = new OrderItem();
        item.setId(99L);
        item.setOrderId(1L);
        Order order = new Order();
        order.setId(1L);
        order.setUserId(3001L);
        order.setPatientId(8001L);
        when(orderItemService.getOrderItemById(99L)).thenReturn(item);
        when(orderService.getOrderById(1L)).thenReturn(order);
        when(userPatientRelationService.hasPatientRelation(4001L, 8001L)).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> orderItemManager.getOrderItemById(99L, 4001L));

        assertEquals(5002, ex.getCode());
    }

    @Test
    void getOrderItemById_missingItem_returnsNull() {
        when(orderItemService.getOrderItemById(99L)).thenReturn(null);

        assertNull(orderItemManager.getOrderItemById(99L, 3001L));
    }

}
