package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.OrderTypeConstant;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.constant.type.RoleEnum;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.properties.ScheduledTaskProperties;
import com.yirancrazy.smartmedical.service.DrugInventoryService;
import com.yirancrazy.smartmedical.service.InventoryTransactionService;
import com.yirancrazy.smartmedical.service.OrderService;
import com.yirancrazy.smartmedical.service.OrderStatusLogService;
import com.yirancrazy.smartmedical.service.PrescriptionItemService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.RegistrationStatusLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OrderTimeOutManager 超时联动单测
 * 覆盖：挂号订单超时的状态门控、状态日志与号源释放。
 * @Author: YiRanCrazy@gmail.com
 * @Description: OrderTimeOutManager 单测
 * @Datetime: 2026-10-01 12:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class OrderTimeOutManagerTest {

    private static final long SYSTEM_OPERATOR_ID = 0L;
    private static final String SYSTEM_OPERATOR_ROLE = RoleEnum.SYSTEM.getRole();

    @Mock private OrderService orderService;
    @Mock private OrderStatusLogService orderStatusLogService;
    @Mock private PrescriptionService prescriptionService;
    @Mock private PrescriptionItemService prescriptionItemService;
    @Mock private DrugInventoryService drugInventoryService;
    @Mock private InventoryTransactionService inventoryTransactionService;
    @Mock private RegistrationService registrationService;
    @Mock private RegistrationScheduleService registrationScheduleService;
    @Mock private RegistrationStatusLogService registrationStatusLogService;
    @Mock private ScheduledTaskProperties scheduledTaskProperties;

    @InjectMocks
    private OrderTimeOutManager orderTimeOutManager;

    /**
     * 超时挂号订单：门控成功后写状态日志并释放号源
     */
    @Test
    void closeExpiredNotPayOrders_registrationOrder_cancelsAndReleasesQuota() {
        Order order = buildExpiredRegistrationOrder();
        Registration reg = buildWaitingPaymentRegistration();

        when(orderService.listExpiredNotPayOrders(any(LocalDateTime.class))).thenReturn(List.of(order));
        when(orderService.cancelOrderIfWaitingPayment(order.getId())).thenReturn(1);
        when(registrationService.getRegistrationByOrderId(order.getId())).thenReturn(reg);
        when(registrationService.updateStatusIfCurrent(reg, RegistrationStatusEnum.CANCELED.getCode()))
                .thenReturn(true);

        orderTimeOutManager.closeExpiredNotPayOrders();

        verify(orderStatusLogService).addOrderStatusLog(any(OrderStatusLog.class));
        verify(registrationService).updateStatusIfCurrent(reg, RegistrationStatusEnum.CANCELED.getCode());
        verify(registrationStatusLogService).writeLog(
                reg.getId(),
                RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode(),
                RegistrationStatusEnum.CANCELED.getCode(),
                SYSTEM_OPERATOR_ID, SYSTEM_OPERATOR_ROLE, "订单支付超时自动取消");
        verify(registrationScheduleService).releaseQuota(reg.getRegistrationScheduleId());
    }

    /**
     * 超时挂号订单：乐观门控失败不写日志、不释放号源
     */
    @Test
    void closeExpiredNotPayOrders_statusGuardFailed_skipsLogAndQuota() {
        Order order = buildExpiredRegistrationOrder();
        Registration reg = buildWaitingPaymentRegistration();

        when(orderService.listExpiredNotPayOrders(any(LocalDateTime.class))).thenReturn(List.of(order));
        when(orderService.cancelOrderIfWaitingPayment(order.getId())).thenReturn(1);
        when(registrationService.getRegistrationByOrderId(order.getId())).thenReturn(reg);
        when(registrationService.updateStatusIfCurrent(reg, RegistrationStatusEnum.CANCELED.getCode()))
                .thenReturn(false);

        orderTimeOutManager.closeExpiredNotPayOrders();

        verify(orderStatusLogService).addOrderStatusLog(any(OrderStatusLog.class));
        verify(registrationService).updateStatusIfCurrent(reg, RegistrationStatusEnum.CANCELED.getCode());
        verify(registrationStatusLogService, never()).writeLog(any(), any(), any(), any(), any(), any());
        verify(registrationScheduleService, never()).releaseQuota(any());
    }

    /**
     * 超时挂号订单：关联挂号状态非待支付时不做状态更新
     */
    @Test
    void closeExpiredNotPayOrders_registrationAlreadyPaid_skipsCancellation() {
        Order order = buildExpiredRegistrationOrder();
        Registration reg = buildWaitingPaymentRegistration();
        reg.setStatus(RegistrationStatusEnum.SUCCESS.getCode());

        when(orderService.listExpiredNotPayOrders(any(LocalDateTime.class))).thenReturn(List.of(order));
        when(orderService.cancelOrderIfWaitingPayment(order.getId())).thenReturn(1);
        when(registrationService.getRegistrationByOrderId(order.getId())).thenReturn(reg);

        orderTimeOutManager.closeExpiredNotPayOrders();

        verify(registrationService, never()).updateStatusIfCurrent(any(), anyInt());
        verify(registrationStatusLogService, never()).writeLog(any(), any(), any(), any(), any(), any());
        verify(registrationScheduleService, never()).releaseQuota(any());
    }

    /**
     * 超时处方订单：门控成功后释放锁定库存并写解锁流水
     */
    @Test
    void closeExpiredNotPayOrders_prescriptionOrder_cancelsAndReleasesStock() {
        Order order = buildExpiredPrescriptionOrder();
        Prescription rx = buildPendingPaymentPrescription();
        PrescriptionItem item = new PrescriptionItem();
        item.setDrugId(7001L);
        item.setQuantity(2);
        DrugInventory inv = new DrugInventory();
        inv.setId(8001L);
        inv.setDrugId(7001L);
        inv.setWarehouseId(9001L);
        inv.setAvailableQuantity(10);
        inv.setLockedQuantity(2);

        when(orderService.listExpiredNotPayOrders(any(LocalDateTime.class))).thenReturn(List.of(order));
        when(orderService.cancelOrderIfWaitingPayment(order.getId())).thenReturn(1);
        when(prescriptionService.getByOrderId(order.getId())).thenReturn(rx);
        when(prescriptionService.cancelPendingIfCurrent(rx.getId())).thenReturn(true);
        when(prescriptionItemService.listByPrescriptionId(rx.getId())).thenReturn(List.of(item));
        when(drugInventoryService.listByDrugIdsForUpdate(List.of(7001L))).thenReturn(List.of(inv));

        orderTimeOutManager.closeExpiredNotPayOrders();

        verify(prescriptionService).cancelPendingIfCurrent(rx.getId());
        verify(drugInventoryService).releaseInventory(8001L, 2);
        verify(inventoryTransactionService).insertInventoryTransaction(any(InventoryTransaction.class));
    }

    /**
     * 超时处方订单：乐观门控失败不释放库存
     */
    @Test
    void closeExpiredNotPayOrders_prescriptionGuardFailed_skipsStockRelease() {
        Order order = buildExpiredPrescriptionOrder();
        Prescription rx = buildPendingPaymentPrescription();

        when(orderService.listExpiredNotPayOrders(any(LocalDateTime.class))).thenReturn(List.of(order));
        when(orderService.cancelOrderIfWaitingPayment(order.getId())).thenReturn(1);
        when(prescriptionService.getByOrderId(order.getId())).thenReturn(rx);
        when(prescriptionService.cancelPendingIfCurrent(rx.getId())).thenReturn(false);

        orderTimeOutManager.closeExpiredNotPayOrders();

        verify(prescriptionService).cancelPendingIfCurrent(rx.getId());
        verify(prescriptionItemService, never()).listByPrescriptionId(any());
        verify(drugInventoryService, never()).releaseInventory(any(), anyInt());
        verify(inventoryTransactionService, never()).insertInventoryTransaction(any());
    }

    /**
     * 超时处方订单：关联处方非待支付时不做状态更新
     */
    @Test
    void closeExpiredNotPayOrders_prescriptionAlreadyPaid_skipsCancellation() {
        Order order = buildExpiredPrescriptionOrder();
        Prescription rx = buildPendingPaymentPrescription();
        rx.setStatus(PrescriptionStatus.PAID.getCode());

        when(orderService.listExpiredNotPayOrders(any(LocalDateTime.class))).thenReturn(List.of(order));
        when(orderService.cancelOrderIfWaitingPayment(order.getId())).thenReturn(1);
        when(prescriptionService.getByOrderId(order.getId())).thenReturn(rx);

        orderTimeOutManager.closeExpiredNotPayOrders();

        verify(prescriptionService, never()).cancelPendingIfCurrent(any());
        verify(drugInventoryService, never()).releaseInventory(any(), anyInt());
    }

    private Order buildExpiredRegistrationOrder() {
        Order order = new Order();
        order.setId(5001L);
        order.setSn(5001L);
        order.setOrderTypeId(OrderTypeConstant.REGISTRATION);
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());
        order.setCreateTime(LocalDateTime.now().minusMinutes(31));
        return order;
    }

    private Registration buildWaitingPaymentRegistration() {
        Registration reg = new Registration();
        reg.setId(1001L);
        reg.setUserId(3001L);
        reg.setRegistrationScheduleId(5001L);
        reg.setStatus(RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode());
        return reg;
    }

    private Order buildExpiredPrescriptionOrder() {
        Order order = new Order();
        order.setId(6001L);
        order.setSn(6001L);
        order.setOrderTypeId(OrderTypeConstant.DRUG);
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());
        order.setCreateTime(LocalDateTime.now().minusMinutes(31));
        return order;
    }

    private Prescription buildPendingPaymentPrescription() {
        Prescription rx = new Prescription();
        rx.setId(7001L);
        rx.setOrderId(6001L);
        rx.setStatus(PrescriptionStatus.PENDING_PAYMENT.getCode());
        return rx;
    }
}
