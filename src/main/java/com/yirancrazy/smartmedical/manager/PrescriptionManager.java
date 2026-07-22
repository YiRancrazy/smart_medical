package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.DrugInventoryMapper;
import com.yirancrazy.smartmedical.pojo.Drug;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.PrescriptionItemRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.SubmitPrescriptionRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.PrescriptionSubmitVO;
import com.yirancrazy.smartmedical.service.DrugService;
import com.yirancrazy.smartmedical.service.InventoryTransactionService;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.OrderItemService;
import com.yirancrazy.smartmedical.service.OrderService;
import com.yirancrazy.smartmedical.service.PrescriptionItemService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.RegistrationStatusLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 处方业务编排
 * @Author: YiRanCrazy@gmail.com
 * @Description: 提交病历 + 开处方大事务入口；支付回调；医生作废
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class PrescriptionManager {

    /** 订单类型:药品订单(DDL order_type.id=2) */
    private static final long ORDER_TYPE_DRUG = 2L;
    /** 项目类型:药品(DDL production_type.id=1) */
    private static final long PRODUCTION_TYPE_DRUG = 1L;
    /** 库存异动类型:锁定 */
    private static final int TXN_LOCK = 4;
    /** 库存异动类型:解锁 */
    private static final int TXN_UNLOCK = 5;

    private final RegistrationService registrationService;
    private final RegistrationScheduleTemplateService registrationScheduleTemplateService;
    private final MedicalRecordService medicalRecordService;
    private final PrescriptionService prescriptionService;
    private final PrescriptionItemService prescriptionItemService;
    private final DrugService drugService;
    private final DrugInventoryMapper drugInventoryMapper;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final InventoryTransactionService inventoryTransactionService;
    private final RegistrationStatusLogManager statusLogManager;
    private final RegistrationStatusLogService registrationStatusLogService;

    /**
     * 医生提交病历 + 开处方（最大事务）
     * @param regId 挂号记录ID
     * @param req 病历 + 处方请求
     * @param doctorId 当前医生ID
     * @return 提交结果(病历/处方/订单/金额)
     * @throws BizException REGISTRATION_NOT_FOUND / DOCTOR_NOT_MATCH / REGISTRATION_STATUS_INVALID / DRUG_NOT_FOUND / DRUG_INVENTORY_INSUFFICIENT
     */
    @Transactional(rollbackFor = Exception.class)
    public PrescriptionSubmitVO submit(Long regId, SubmitPrescriptionRequest req, Long doctorId) {
        // 1. 校验挂号存在 + ownership + status
        Registration reg = registrationService.getRegistrationById(regId);
        if (reg == null) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_FOUND);
        }
        RegistrationScheduleTemplate template = registrationScheduleTemplateService
                .getRegistrationScheduleTemplateById(reg.getRegistrationScheduleId());
        Long regDoctorId = template == null ? null : template.getDoctorId();
        if (!doctorId.equals(regDoctorId)) {
            throw new BizException(BizErrorCode.DOCTOR_NOT_MATCH);
        }
        Integer curStatus = reg.getStatus();
        if (curStatus != RegistrationStatusEnum.IN_TREATMENT.getCode()
                && curStatus != RegistrationStatusEnum.PENDING_PAYMENT.getCode()) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID, "请先完成叫号");
        }

        // 2. 校验所有药品 + 库存 + 累计金额
        int totalAmount = 0;
        for (PrescriptionItemRequest item : req.getItems()) {
            Drug drug = drugService.getDrugById(item.getDrugId());
            if (drug == null) {
                throw new BizException(BizErrorCode.DRUG_NOT_FOUND, "drugId=" + item.getDrugId());
            }
            DrugInventory inv = drugInventoryMapper.selectOne(
                    new LambdaQueryWrapper<DrugInventory>()
                            .eq(DrugInventory::getDrugId, item.getDrugId())
                            .last("LIMIT 1"));
            if (inv == null || inv.getAvailableQuantity() < item.getQuantity()) {
                throw new BizException(BizErrorCode.DRUG_INVENTORY_INSUFFICIENT,
                        "drugName=" + drug.getCommonName()
                                + ", available=" + (inv == null ? 0 : inv.getAvailableQuantity())
                                + ", required=" + item.getQuantity());
            }
            totalAmount += drug.getPrice() * item.getQuantity();
        }

        // 3. 创建/更新病历 (status=1 已提交)
        MedicalRecord record = medicalRecordService.getOne(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getRegistrationId, regId)
                        .last("LIMIT 1"));
        if (record == null) {
            record = new MedicalRecord();
            record.setId(IdUtil.getSnowflakeNextId());
            record.setRegistrationId(regId);
            record.setDoctorId(doctorId);
            record.setPatientId(reg.getUserId());
        }
        record.setChiefComplaint(req.getChiefComplaint());
        record.setPresentIllness(req.getPresentIllness());
        record.setPastHistory(req.getPastHistory());
        record.setPhysicalExam(req.getPhysicalExam());
        record.setDiagnosis(req.getDiagnosis());
        record.setTreatmentPlan(req.getTreatmentPlan());
        record.setStatus(1);
        if (record.getId() == null) {
            medicalRecordService.save(record);
        } else {
            medicalRecordService.updateById(record);
        }

        // 4. 创建处方头 (status=0 待支付)
        Prescription rx = new Prescription();
        rx.setId(IdUtil.getSnowflakeNextId());
        rx.setMedicalRecordId(record.getId());
        rx.setTotalAmount(totalAmount);
        rx.setStatus(PrescriptionStatus.PENDING_PAYMENT.getCode());
        prescriptionService.save(rx);

        // 5. 创建药品订单 (status=0 待支付)
        Order order = new Order();
        order.setId(IdUtil.getSnowflakeNextId());
        order.setUserId(reg.getUserId());
        order.setOrderTypeId(ORDER_TYPE_DRUG);
        order.setSn(System.currentTimeMillis());
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());
        order.setTotalAmount(totalAmount);
        order.setOrderCreateTime(LocalDateTime.now());
        orderService.insertOrder(order);

        // 关联订单ID到处方
        rx.setOrderId(order.getId());
        prescriptionService.updateById(rx);

        // 6. 逐条:处方明细 + 订单明细 + 锁定库存 + 流水
        for (PrescriptionItemRequest item : req.getItems()) {
            Drug drug = drugService.getDrugById(item.getDrugId());

            PrescriptionItem rxItem = new PrescriptionItem();
            rxItem.setId(IdUtil.getSnowflakeNextId());
            rxItem.setPrescriptionId(rx.getId());
            rxItem.setDrugId(item.getDrugId());
            rxItem.setQuantity(item.getQuantity());
            rxItem.setUnitPrice(drug.getPrice());
            rxItem.setUsageMethod(item.getUsageMethod());
            prescriptionItemService.save(rxItem);

            OrderItem orderItem = new OrderItem();
            orderItem.setId(IdUtil.getSnowflakeNextId());
            orderItem.setOrderId(order.getId());
            orderItem.setProductionId(item.getDrugId());
            orderItem.setProductionTypeId(PRODUCTION_TYPE_DRUG);
            orderItem.setQuantity(item.getQuantity());
            orderItem.setProductionName(drug.getCommonName());
            orderItemService.insertOrderItem(orderItem);

            // 锁定库存(累加 locked,available 不变)
            DrugInventory inv = drugInventoryMapper.selectOne(
                    new LambdaQueryWrapper<DrugInventory>()
                            .eq(DrugInventory::getDrugId, item.getDrugId())
                            .last("LIMIT 1"));
            inv.setLockedQuantity(inv.getLockedQuantity() + item.getQuantity());
            drugInventoryMapper.updateById(inv);

            InventoryTransaction txn = new InventoryTransaction();
            txn.setId(IdUtil.getSnowflakeNextId());
            txn.setDrugId(item.getDrugId());
            txn.setWarehouseId(inv.getWarehouseId());
            txn.setTransactionType(TXN_LOCK);
            txn.setRelatedOrder(String.valueOf(order.getSn()));
            txn.setQuantityChange(item.getQuantity());
            txn.setQuantityBefore(inv.getAvailableQuantity());
            txn.setQuantityAfter(inv.getAvailableQuantity());
            txn.setOperatorId(doctorId);
            txn.setOperatorName("doctor");
            inventoryTransactionService.insertInventoryTransaction(txn);
        }

        // 7. registration 状态迁移:就诊中 → 待支付(处方补缴)
        statusLogManager.transition(reg,
                RegistrationStatusEnum.PENDING_PAYMENT.getCode(),
                doctorId, "doctor", "提交病历开方");

        // 8. 构造返回 VO
        PrescriptionSubmitVO vo = new PrescriptionSubmitVO();
        vo.setMedicalRecordId(record.getId());
        vo.setPrescriptionId(rx.getId());
        vo.setOrderId(order.getId());
        vo.setOrderSn(String.valueOf(order.getSn()));
        vo.setTotalAmount(totalAmount);
        vo.setRegistrationStatus(RegistrationStatusEnum.PENDING_PAYMENT.getCode());
        return vo;
    }

    /**
     * 支付成功回调:标记处方为已支付(由 PaymentRecordManager 调用)
     * @param orderId 订单ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void markAsPaid(Long orderId) {
        Prescription rx = prescriptionService.getOne(
                new LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getOrderId, orderId)
                        .last("LIMIT 1"));
        if (rx == null) {
            log.warn("[prescription-paid] no prescription for orderId={}", orderId);
            return;
        }
        // @Version 乐观锁会自动校验
        rx.setStatus(PrescriptionStatus.PAID.getCode());
        prescriptionService.updateById(rx);

        // 写一条挂号状态日志(合成:from=to=当前状态,operator=system)
        // 用于完整记录"处方已支付"事件，满足 spec §6.3 的"日志记录所有事件"约束
        if (rx.getMedicalRecordId() != null) {
            MedicalRecord record = medicalRecordService.getById(rx.getMedicalRecordId());
            if (record != null) {
                Registration reg = registrationService.getRegistrationById(record.getRegistrationId());
                if (reg != null) {
                    registrationStatusLogService.writeLog(
                            reg.getId(),
                            reg.getStatus(),
                            reg.getStatus(),
                            0L,
                            "system",
                            "处方已支付");
                }
            }
        }

        log.info("[prescription-paid] prescriptionId={}, orderId={}", rx.getId(), orderId);
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

        // 释放锁定库存
        List<PrescriptionItem> items = prescriptionItemService.list(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescriptionId));
        for (PrescriptionItem item : items) {
            DrugInventory inv = drugInventoryMapper.selectOne(
                    new LambdaQueryWrapper<DrugInventory>()
                            .eq(DrugInventory::getDrugId, item.getDrugId())
                            .last("LIMIT 1"));
            if (inv != null) {
                inv.setLockedQuantity(inv.getLockedQuantity() - item.getQuantity());
                drugInventoryMapper.updateById(inv);
            }
            InventoryTransaction txn = new InventoryTransaction();
            txn.setId(IdUtil.getSnowflakeNextId());
            txn.setDrugId(item.getDrugId());
            txn.setWarehouseId(inv != null ? inv.getWarehouseId() : null);
            txn.setTransactionType(TXN_UNLOCK);
            txn.setRelatedOrder(String.valueOf(rx.getOrderId()));
            txn.setQuantityChange(-item.getQuantity());
            txn.setOperatorId(doctorId);
            txn.setOperatorName("doctor");
            inventoryTransactionService.insertInventoryTransaction(txn);
        }

        // 关闭订单
        if (rx.getOrderId() != null) {
            Order order = orderService.getOrderById(rx.getOrderId());
            if (order != null) {
                order.setStatus(OrderStatus.CANCELED.getCode());
                orderService.updateOrderById(order);
            }
        }

        // 处方置为已取消
        rx.setStatus(PrescriptionStatus.CANCELLED.getCode());
        prescriptionService.updateById(rx);

        // registration 回退到 IN_TREATMENT (就诊中)
        if (record != null) {
            Registration reg = registrationService.getRegistrationById(record.getRegistrationId());
            if (reg != null && reg.getStatus() == RegistrationStatusEnum.PENDING_PAYMENT.getCode()) {
                statusLogManager.transition(reg,
                        RegistrationStatusEnum.IN_TREATMENT.getCode(),
                        doctorId, "doctor", "作废处方");
            }
        }
    }
}