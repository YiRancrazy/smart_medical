package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.service.OrderStatusLogService;
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
 * OrderStatusLogManager 单测
 */
@ExtendWith(MockitoExtension.class)
class OrderStatusLogManagerTest {

    @Mock private OrderStatusLogService orderStatusLogService;

    @InjectMocks
    private OrderStatusLogManager orderStatusLogManager;

    @Test
    void addOrderStatusLog_setsSnowflakeIdAndDelegates() {
        when(orderStatusLogService.insertOrderStatusLog(any(OrderStatusLog.class))).thenReturn(1);
        OrderStatusLog log = new OrderStatusLog();

        int rows = orderStatusLogManager.addOrderStatusLog(log);

        assertEquals(1, rows);
        assertNotNull(log.getId());
        verify(orderStatusLogService).insertOrderStatusLog(log);
    }

    @Test
    void getOrderStatusLogById_delegates() {
        OrderStatusLog log = new OrderStatusLog();
        log.setId(7L);
        when(orderStatusLogService.getOrderStatusLogById(7L)).thenReturn(log);

        OrderStatusLog result = orderStatusLogManager.getOrderStatusLogById(7L);

        assertEquals(7L, result.getId());
    }
}
