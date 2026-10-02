package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.constant.type.RoleEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.pojo.PaymentRecord;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.service.DrugInventoryService;
import com.yirancrazy.smartmedical.service.InventoryTransactionService;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.OrderService;
import com.yirancrazy.smartmedical.service.OrderStatusLogService;
import com.yirancrazy.smartmedical.service.PaymentRecordService;
import com.yirancrazy.smartmedical.service.PrescriptionItemService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.RegistrationStatusLogService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 处方状态流转业务编排
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户退款、医生作废及库存释放
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class PrescriptionLifecycleManager {

    /** 库存异动类型:解锁 */
    private static final int TXN_UNLOCK = 5;
    /** 支付记录状态:已退款 */
    private static final int PAYMENT_STATUS_REFUNDED = 4;
    /** 默认支付方式:4 现金 */
    private static final int DEFAULT_PAYMENT_METHOD_ID = 4;

    private final MedicalRecordService medicalRecordService;
    private final PrescriptionService prescriptionService;
    private final PrescriptionItemService prescriptionItemService;
    private final DrugInventoryService drugInventoryService;
    private final OrderService orderService;
    private final InventoryTransactionService inventoryTransactionService;
    private final PaymentRecordService paymentRecordService;
    private final RegistrationStatusLogService registrationStatusLogService;
    private final OrderStatusLogService orderStatusLogService;
    private final UserPatientRelationService userPatientRelationService;
    private final RegistrationService registrationService;

    /**
     * 用户退款已支付处方(仅 status=1 已支付、未发药):释放锁定库存 + 写退款记录 + 订单置为已退款 + 处方置为已取消
     * @param prescriptionId 处方ID
     * @param userId 当前用户ID
     * @throws BizException PRESCRIPTION_NOT_FOUND / PRESCRIPTION_NOT_OWNED / PRESCRIPTION_ALREADY_DISPENSED
     */
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long prescriptionId, Long userId) {
        Prescription rx = prescriptionService.getById(prescriptionId);
        if (rx == null) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        MedicalRecord record = rx.getMedicalRecordId() == null
                ? null : medicalRecordService.getById(rx.getMedicalRecordId());
        if (record == null || !userPatientRelationService.listAccessiblePatientUserIds(userId).contains(record.getPatientId())) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_OWNED);
        }
        // 仅已支付可退，已取消/已发药拒绝（已发药应走药师逆向）
        if (rx.getStatus() == null || rx.getStatus() != PrescriptionStatus.PAID.getCode()) {
            throw new BizException(BizErrorCode.PRESCRIPTION_ALREADY_DISPENSED, "仅已支付未发药的处方可退款");
        }

        // 1. 订单退款（已支付才退），写退款记录 + 原支付记录置为已退款 + 订单置为已退款
        if (rx.getOrderId() != null) {
            Order order = orderService.getOrderById(rx.getOrderId());
            if (order != null && order.getStatus() != null
                    && order.getStatus() == OrderStatus.PAID.getCode()) {
                PaymentRecord orig = paymentRecordService.getSuccessPaymentRecordByOrderId(order.getId());
                int refundAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0;
                if (refundAmount > 0) {
                    PaymentRecord refund = new PaymentRecord();
                    refund.setId(IdUtil.getSnowflakeNextId());
                    refund.setOrderId(order.getId());
                    refund.setSn(System.currentTimeMillis());
                    refund.setTotalAmount(-refundAmount);
                    refund.setRealAmount(-refundAmount);
                    refund.setPaymentMethodId(orig != null && orig.getPaymentMethodId() != null
                            ? orig.getPaymentMethodId() : DEFAULT_PAYMENT_METHOD_ID);
                    refund.setStatus(PAYMENT_STATUS_REFUNDED);
                    refund.setPaymentTime(LocalDateTime.now());
                    paymentRecordService.insertPaymentRecord(refund);
                }
                if (orig != null) {
                    orig.setStatus(PAYMENT_STATUS_REFUNDED);
                    paymentRecordService.updatePaymentRecordById(orig);
                }
                Integer fromStatus = order.getStatus();
                order.setStatus(OrderStatus.REFUNDED.getCode());
                orderService.updateOrderById(order);
                OrderStatusLog orderLog = new OrderStatusLog();
                orderLog.setOrderId(order.getId());
                orderLog.setFromStatus(fromStatus);
                orderLog.setToStatus(OrderStatus.REFUNDED.getCode());
                orderLog.setOperatorId(userId);
                orderLog.setOperatorRole(RoleEnum.PATIENT.getRole());
                orderLog.setRemark("处方退款");
                orderStatusLogService.addOrderStatusLog(orderLog);
            }
        }

        // 2. 处方置为已取消：条件更新仅当仍处于已支付才生效，并发退款只有一个成功，失败方抛异常回滚
        boolean rxUpdated = prescriptionService.applyRefundIfCurrent(rx.getId());
        if (!rxUpdated) {
            throw new BizException(BizErrorCode.PRESCRIPTION_ALREADY_CANCELLED, "处方已被处理，请刷新");
        }
        // 3. 释放锁定库存：locked -= q, available += q（FOR UPDATE 行锁 + 状态守卫，防并发双释放）
        releaseLockedStock(rx, userId, RoleEnum.PATIENT.getRole(), "处方退款释放库存");
        log.info("[prescription-refund] prescriptionId={}, orderId={}, userId={}", prescriptionId, rx.getOrderId(), userId);
    }

    /**
     * 医生作废处方(仅 status=0 待支付时):释放库存 + 关闭订单 + 处方置为已取消 + registration 回退到就诊中
     * @param prescriptionId 处方ID
     * @param doctorId 医生ID
     * @throws BizException PRESCRIPTION_NOT_FOUND / PRESCRIPTION_NOT_OWNED / PRESCRIPTION_ALREADY_DISPENSED
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelByDoctor(Long prescriptionId, Long doctorId) {
        Prescription rx = prescriptionService.getById(prescriptionId);
        if (rx == null) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        // ownership:反查病历的 doctor_id
        MedicalRecord record = rx.getMedicalRecordId() == null
                ? null : medicalRecordService.getById(rx.getMedicalRecordId());
        if (record == null || !doctorId.equals(record.getDoctorId())) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_OWNED);
        }
        if (rx.getStatus() != PrescriptionStatus.PENDING_PAYMENT.getCode()) {
            throw new BizException(BizErrorCode.PRESCRIPTION_ALREADY_DISPENSED, "只能作废待支付处方");
        }

        // 处方置为已取消：条件更新仅当仍处于待支付才生效，先抢到者生效，失败方抛异常回滚
        boolean rxUpdated = prescriptionService.cancelPendingIfCurrent(rx.getId());
        if (!rxUpdated) {
            throw new BizException(BizErrorCode.PRESCRIPTION_ALREADY_CANCELLED, "处方已被处理，请刷新");
        }

        // 释放锁定库存：locked -= q, available += q（FOR UPDATE 行锁，防并发双释放）
        releaseLockedStock(rx, doctorId, RoleEnum.DOCTOR.getRole(), "作废处方释放库存");

        // 关闭订单
        closeOrderForCancel(rx, doctorId, RoleEnum.DOCTOR.getRole(), "作废处方关闭订单");

        // registration 状态回退：仅旧流程产生的 PENDING_PAYMENT 回退到就诊中；
        // 新流程提交后挂号已完成，作废处方不再回退挂号状态
        if (record != null) {
            Registration reg = registrationService.getRegistrationById(record.getRegistrationId());
            if (reg != null && reg.getStatus() == RegistrationStatusEnum.PENDING_PAYMENT.getCode()) {
                Integer fromStatus = reg.getStatus();
                int toStatus = RegistrationStatusEnum.IN_TREATMENT.getCode();
                if (!registrationService.updateStatusIfCurrent(reg, toStatus)) {
                    throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID, "状态已变更，请刷新");
                }
                registrationStatusLogService.writeLog(reg.getId(), fromStatus, toStatus,
                        doctorId, RoleEnum.DOCTOR.getRole(), "作废处方");
            }
        }
    }

    /**
     * 关闭处方关联订单（置为已取消并写订单状态日志）
     */
    private void closeOrderForCancel(Prescription rx, Long operatorId, String operatorRole, String remark) {
        if (rx.getOrderId() == null) {
            return;
        }
        Order order = orderService.getOrderById(rx.getOrderId());
        if (order == null) {
            return;
        }
        Integer fromStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELED.getCode());
        orderService.updateOrderById(order);
        OrderStatusLog orderLog = new OrderStatusLog();
        orderLog.setOrderId(order.getId());
        orderLog.setFromStatus(fromStatus);
        orderLog.setToStatus(OrderStatus.CANCELED.getCode());
        orderLog.setOperatorId(operatorId);
        orderLog.setOperatorRole(operatorRole);
        orderLog.setRemark(remark);
        orderStatusLogService.addOrderStatusLog(orderLog);
    }

    /**
     * 释放处方锁定的库存并写解锁流水（FOR UPDATE 行锁串行化防并发双释放）
     * @param rx 处方
     * @param operatorId 操作人ID（系统传 0L）
     * @param operatorName 操作人角色（doctor/user/system）
     * @param remark 流水备注
     */
    private void releaseLockedStock(Prescription rx, Long operatorId, String operatorName, String remark) {
        List<PrescriptionItem> items = prescriptionItemService.listByPrescriptionId(rx.getId());
        List<Long> drugIds = items.stream()
                .map(PrescriptionItem::getDrugId)
                .distinct()
                .collect(Collectors.toList());
        if (drugIds.isEmpty()) {
            return;
        }
        Map<Long, DrugInventory> inventoryMap = drugInventoryService.listByDrugIdsForUpdate(drugIds)
                .stream().collect(Collectors.toMap(DrugInventory::getDrugId, inv -> inv, (i1, i2) -> i1));
        for (PrescriptionItem item : items) {
            DrugInventory inv = inventoryMap.get(item.getDrugId());
            if (inv == null) {
                continue;
            }
            int lockedBefore = inv.getLockedQuantity() == null ? 0 : inv.getLockedQuantity();
            if (lockedBefore < item.getQuantity()) {
                log.warn("[inventory-release] drugId={} locked={} < qty={}，已释放过，跳过",
                        item.getDrugId(), lockedBefore, item.getQuantity());
                continue;
            }
            int qtyBefore = inv.getAvailableQuantity() == null ? 0 : inv.getAvailableQuantity();
            drugInventoryService.releaseInventory(inv.getId(), item.getQuantity());
            InventoryTransaction txn = new InventoryTransaction();
            txn.setId(IdUtil.getSnowflakeNextId());
            txn.setDrugId(item.getDrugId());
            txn.setWarehouseId(inv.getWarehouseId());
            txn.setTransactionType(TXN_UNLOCK);
            txn.setRelatedOrder(String.valueOf(rx.getOrderId()));
            txn.setQuantityChange(-item.getQuantity());
            txn.setQuantityBefore(qtyBefore);
            txn.setQuantityAfter(qtyBefore + item.getQuantity());
            txn.setRemark(remark);
            txn.setOperatorId(operatorId);
            txn.setOperatorName(operatorName);
            inventoryTransactionService.insertInventoryTransaction(txn);
            // 更新本地值，同一处方多明细同药时后续判断准确
            inv.setLockedQuantity(lockedBefore - item.getQuantity());
            inv.setAvailableQuantity(qtyBefore + item.getQuantity());
        }
    }
}
