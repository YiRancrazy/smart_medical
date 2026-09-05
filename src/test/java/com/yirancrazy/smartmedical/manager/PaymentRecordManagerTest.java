package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.mapper.OrdersMapper;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.service.*;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.pojo.Registration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PaymentRecordManager 单测
 * 覆盖 BUG-B03: 支付回调幂等（DB 唯一索引 + 订单状态守卫，无 Redis 前置标记）
 */
@ExtendWith(MockitoExtension.class)
class PaymentRecordManagerTest {

    @InjectMocks
    private PaymentRecordManager paymentRecordManager;

    @Mock
    private PaymentRecordService paymentRecordService;
    @Mock
    private PatientCardService patientCardService;
    @Mock
    private PatientService patientService;
    @Mock
    private OrderService orderService;
    @Mock
    private OrdersMapper ordersMapper;
    @Mock
    private OrderTypeService orderTypeService;
    @Mock
    private OrderItemService orderItemService;
    @Mock
    private ProductionTypeService productionTypeService;
    @Mock
    private PayMethodService payMethodService;
    @Mock
    private PrescriptionManager prescriptionManager;
    @Mock
    private RegistrationService registrationService;
    @Mock
    private RegistrationStatusLogManager registrationStatusLogManager;
    @Mock
    private OrderStatusLogManager orderStatusLogManager;

    @Test
    void paySuccess_duplicateTransactionSn_marksOrderPaidIdempotent() {
        Long orderId = 5001L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(1001L);
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());
        order.setTotalAmount(100);

        when(orderService.getOrderById(orderId)).thenReturn(order);
        doThrow(new DuplicateKeyException("uk_transaction_sn"))
                .when(paymentRecordService).insertPaymentRecord(any());
        when(ordersMapper.update(any(), any())).thenReturn(1);
        when(registrationService.getRegistrationByOrderId(orderId)).thenReturn(null);
        doNothing().when(prescriptionManager).markAsPaid(orderId);

        Result<Void> result = paymentRecordManager.paySuccess(orderId, 1001L, 1, 9876543210L, 100);

        assertEquals(200, result.getCode());
        // 重复流水号被 DB 唯一索引拦下（幂等），但订单仍应被置为已支付，避免漏单
        verify(ordersMapper).update(any(), any());
    }

    // ===== paySuccess() 测试 =====

    @Test
    void paySuccess_happyPath_createsPaymentRecordAndUpdatesOrder() {
        Long orderId = 5001L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(1001L);
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());
        order.setTotalAmount(5000);

        when(orderService.getOrderById(orderId)).thenReturn(order);
        when(ordersMapper.update(any(), any())).thenReturn(1);
        when(registrationService.getRegistrationByOrderId(orderId)).thenReturn(null);
        doNothing().when(prescriptionManager).markAsPaid(orderId);

        Result<Void> result = paymentRecordManager.paySuccess(orderId, 1001L, 1, 9876543210L, 5000);

        assertEquals(200, result.getCode());
        verify(ordersMapper).update(any(), any());
        verify(orderStatusLogManager).addOrderStatusLog(any(OrderStatusLog.class));
        verify(registrationService).getRegistrationByOrderId(orderId);
        verify(prescriptionManager).markAsPaid(orderId);
    }

    @Test
    void paySuccess_orderNotFound_throwsException() {
        Long orderId = 5001L;
        when(orderService.getOrderById(orderId)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> paymentRecordManager.paySuccess(orderId, 1001L, 1, 9876543210L, 5000));
        assertEquals(BizErrorCode.ORDER_STATUS_INVALID.getCode(), ex.getCode());
    }

    @Test
    void paySuccess_wrongUser_throwsException() {
        Long orderId = 5001L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(9999L);
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());

        when(orderService.getOrderById(orderId)).thenReturn(order);

        BizException ex = assertThrows(BizException.class,
                () -> paymentRecordManager.paySuccess(orderId, 1001L, 1, 9876543210L, 5000));
        assertEquals(BizErrorCode.ORDER_STATUS_INVALID.getCode(), ex.getCode());
    }

    @Test
    void paySuccess_amountMismatch_throwsException() {
        Long orderId = 5001L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(1001L);
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());
        order.setTotalAmount(5000);

        when(orderService.getOrderById(orderId)).thenReturn(order);

        BizException ex = assertThrows(BizException.class,
                () -> paymentRecordManager.paySuccess(orderId, 1001L, 1, 9876543210L, 4000));
        assertEquals(BizErrorCode.ORDER_STATUS_INVALID.getCode(), ex.getCode());
    }

    @Test
    void paySuccess_alreadyPaid_skipsIdempotent() {
        Long orderId = 5001L;
        Order order = new Order();
        order.setId(orderId);
        order.setUserId(1001L);
        order.setStatus(OrderStatus.PAID.getCode());
        order.setTotalAmount(5000);

        Registration reg = new Registration();
        reg.setId(8001L);
        reg.setStatus(RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode());

        when(orderService.getOrderById(orderId)).thenReturn(order);
        when(registrationService.getRegistrationByOrderId(orderId)).thenReturn(reg);

        Result<Void> result = paymentRecordManager.paySuccess(orderId, 1001L, 1, 9876543210L, 5000);

        assertEquals(200, result.getCode());
        verify(ordersMapper, never()).update(any(), any());
        verify(registrationStatusLogManager).transition(eq(reg), eq(RegistrationStatusEnum.SUCCESS.getCode()),
                eq(0L), eq("system"), eq("支付成功(补同步)"));
    }
}
