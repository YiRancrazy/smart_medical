package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.DrugInventoryMapper;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.Drug;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.User;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.dto.admin.request.PrescriptionQueryRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.PrescriptionItemRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.SubmitPrescriptionRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.DoctorPrescriptionDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.DoctorPrescriptionListVO;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.PrescriptionSubmitVO;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PrescriptionDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PrescriptionListVO;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.DepartmentService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.DrugService;
import com.yirancrazy.smartmedical.service.InventoryTransactionService;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.OrderItemService;
import com.yirancrazy.smartmedical.service.OrderService;
import com.yirancrazy.smartmedical.service.PrescriptionItemService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.RegistrationStatusLogService;
import com.yirancrazy.smartmedical.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final DoctorService doctorService;
    private final DepartmentService departmentService;
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
    private final RegistrationScheduleService registrationScheduleService;
    private final OrderStatusLogManager orderStatusLogManager;
    private final PatientManager patientManager;
    private final UserService userService;
    private final AccountService accountService;

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
        Registration reg = validateRegistration(regId, doctorId);

        // 2. 创建/更新病历 (status=1 已提交)
        MedicalRecord record = saveMedicalRecord(reg, doctorId, req);

        // 无处方药品：直接完成就诊，不创建空处方/订单
        if (req.getItems() == null || req.getItems().isEmpty()) {
            return handleNoPrescription(reg, record, doctorId);
        }

        // 3. 校验所有药品 + 库存 + 累计金额
        DrugValidationResult drugResult = validateDrugsAndBuildCache(req.getItems());

        // 4. 创建处方 + 药品订单
        PrescriptionOrderResult poResult = createPrescriptionAndOrder(reg, record, drugResult.totalAmount);

        // 5. 逐条:处方明细 + 订单明细 + 锁定库存 + 流水
        processDrugItems(req.getItems(), poResult.prescription, poResult.order, doctorId,
                drugResult.drugCache, drugResult.inventoryCache);

        // 6. registration 状态迁移:就诊中 → 完成
        statusLogManager.transition(reg,
                RegistrationStatusEnum.COMPLETED.getCode(),
                doctorId, "doctor", "提交病历开方");

        // 7. 构造返回 VO
        PrescriptionSubmitVO vo = new PrescriptionSubmitVO();
        vo.setMedicalRecordId(record.getId());
        vo.setPrescriptionId(poResult.prescription.getId());
        vo.setOrderId(poResult.order.getId());
        vo.setOrderSn(String.valueOf(poResult.order.getSn()));
        vo.setTotalAmount(drugResult.totalAmount);
        vo.setRegistrationStatus(RegistrationStatusEnum.COMPLETED.getCode());
        return vo;
    }

    // ========== submit() 子方法 ==========

    /**
     * 校验挂号记录存在性、医生归属和状态
     */
    private Registration validateRegistration(Long regId, Long doctorId) {
        Registration reg = registrationService.getRegistrationById(regId);
        if (reg == null) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_FOUND);
        }
        RegistrationSchedule schedule = registrationScheduleService.getRegistrationScheduleById(reg.getRegistrationScheduleId());
        RegistrationScheduleTemplate template = schedule == null ? null
                : registrationScheduleTemplateService.getRegistrationScheduleTemplateById(schedule.getRegistrationScheduleTemplateId());
        Long regDoctorId = template == null ? null : template.getDoctorId();
        if (!doctorId.equals(regDoctorId)) {
            throw new BizException(BizErrorCode.DOCTOR_NOT_MATCH);
        }
        Integer curStatus = reg.getStatus();
        if (curStatus == null
                || curStatus != RegistrationStatusEnum.IN_TREATMENT.getCode()) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID,
                    "仅就诊中状态可提交病历开方，已开方请先作废");
        }
        return reg;
    }

    /**
     * 校验药品存在性和库存充足，返回药品缓存、库存缓存和总金额
     */
    private DrugValidationResult validateDrugsAndBuildCache(List<PrescriptionItemRequest> items) {
        int totalAmount = 0;
        List<Long> drugIds = items.stream().map(PrescriptionItemRequest::getDrugId).distinct().collect(Collectors.toList());
        Map<Long, Drug> drugCache = drugService.listDrugsByIds(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, d -> d));
        Map<Long, DrugInventory> inventoryCache = drugInventoryMapper.selectList(
                new LambdaQueryWrapper<DrugInventory>().in(DrugInventory::getDrugId, drugIds))
                .stream().collect(Collectors.toMap(DrugInventory::getDrugId, inv -> inv, (i1, i2) -> i1));
        for (PrescriptionItemRequest item : items) {
            Drug drug = drugCache.get(item.getDrugId());
            if (drug == null) {
                throw new BizException(BizErrorCode.DRUG_NOT_FOUND, "drugId=" + item.getDrugId());
            }
            DrugInventory inv = inventoryCache.get(item.getDrugId());
            if (inv == null || inv.getAvailableQuantity() < item.getQuantity()) {
                throw new BizException(BizErrorCode.DRUG_INVENTORY_INSUFFICIENT,
                        "drugName=" + drug.getCommonName()
                                + ", available=" + (inv == null ? 0 : inv.getAvailableQuantity())
                                + ", required=" + item.getQuantity());
            }
            totalAmount += drug.getPrice() * item.getQuantity();
        }
        return new DrugValidationResult(drugCache, inventoryCache, totalAmount);
    }

    /**
     * 创建或更新病历记录
     */
    private MedicalRecord saveMedicalRecord(Registration reg, Long doctorId, SubmitPrescriptionRequest req) {
        Long regId = reg.getId();
        MedicalRecord record = medicalRecordService.getOne(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getRegistrationId, regId)
                        .last("LIMIT 1"));
        if (record == null) {
            record = new MedicalRecord();
            // ponytail: 不预填 id，@TableId(ASSIGN_ID) 在 save 时自动生成雪花 id；预填会导致下方 getId()==null 判断失效，新病历误走 updateById 静默失败
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
        return record;
    }

    /**
     * 无处方药品时直接完成就诊
     */
    private PrescriptionSubmitVO handleNoPrescription(Registration reg, MedicalRecord record, Long doctorId) {
        statusLogManager.transition(reg,
                RegistrationStatusEnum.COMPLETED.getCode(),
                doctorId, "doctor", "就诊完成(无处方)");
        PrescriptionSubmitVO vo = new PrescriptionSubmitVO();
        vo.setMedicalRecordId(record.getId());
        vo.setTotalAmount(0);
        vo.setRegistrationStatus(RegistrationStatusEnum.COMPLETED.getCode());
        return vo;
    }

    /**
     * 创建处方头 + 药品订单，并关联订单ID到处方
     */
    private PrescriptionOrderResult createPrescriptionAndOrder(Registration reg, MedicalRecord record, int totalAmount) {
        Prescription rx = new Prescription();
        rx.setId(IdUtil.getSnowflakeNextId());
        rx.setMedicalRecordId(record.getId());
        rx.setTotalAmount(totalAmount);
        rx.setStatus(PrescriptionStatus.PENDING_PAYMENT.getCode());
        prescriptionService.save(rx);

        Order order = new Order();
        order.setId(IdUtil.getSnowflakeNextId());
        order.setUserId(reg.getUserId());
        order.setOrderTypeId(ORDER_TYPE_DRUG);
        order.setSn(IdUtil.getSnowflakeNextId());
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());
        order.setTotalAmount(totalAmount);
        order.setOrderCreateTime(LocalDateTime.now());
        orderService.insertOrder(order);

        rx.setOrderId(order.getId());
        prescriptionService.updateById(rx);

        return new PrescriptionOrderResult(rx, order);
    }

    /**
     * 逐条创建处方明细、订单明细、锁定库存并写入库存异动流水
     */
    private void processDrugItems(List<PrescriptionItemRequest> items, Prescription rx, Order order,
                                   Long doctorId, Map<Long, Drug> drugCache, Map<Long, DrugInventory> inventoryCache) {
        for (PrescriptionItemRequest item : items) {
            Drug drug = drugCache.get(item.getDrugId());

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

            DrugInventory inv = inventoryCache.get(item.getDrugId());
            if (inv == null) {
                throw new BizException(BizErrorCode.DRUG_INVENTORY_INSUFFICIENT,
                        "药品库存不存在：drugId=" + item.getDrugId());
            }
            int qtyBefore = inv.getAvailableQuantity() == null ? 0 : inv.getAvailableQuantity();
            if (qtyBefore < item.getQuantity()) {
                throw new BizException(BizErrorCode.DRUG_INVENTORY_INSUFFICIENT,
                        "可用库存不足：drugId=" + item.getDrugId());
            }
            int rows = drugInventoryMapper.update(null,
                    new UpdateWrapper<DrugInventory>()
                            .eq("id", inv.getId())
                            .ge("available_quantity", item.getQuantity())
                            .setSql("locked_quantity = locked_quantity + " + item.getQuantity())
                            .setSql("available_quantity = available_quantity - " + item.getQuantity()));
            if (rows == 0) {
                throw new BizException(BizErrorCode.DRUG_INVENTORY_INSUFFICIENT,
                        "库存锁定失败(并发)：drugId=" + item.getDrugId());
            }
            inv.setLockedQuantity((inv.getLockedQuantity() == null ? 0 : inv.getLockedQuantity()) + item.getQuantity());
            inv.setAvailableQuantity(qtyBefore - item.getQuantity());

            InventoryTransaction txn = new InventoryTransaction();
            txn.setId(IdUtil.getSnowflakeNextId());
            txn.setDrugId(item.getDrugId());
            txn.setWarehouseId(inv.getWarehouseId());
            txn.setTransactionType(TXN_LOCK);
            txn.setRelatedOrder(String.valueOf(order.getSn()));
            txn.setQuantityChange(item.getQuantity());
            txn.setQuantityBefore(qtyBefore);
            txn.setQuantityAfter(qtyBefore - item.getQuantity());
            txn.setOperatorId(doctorId);
            txn.setOperatorName("doctor");
            inventoryTransactionService.insertInventoryTransaction(txn);
        }
    }

    // ========== 内部类 ==========

    /**
     * 药品校验结果：药品缓存、库存缓存、总金额
     */
    private static class DrugValidationResult {
        private final Map<Long, Drug> drugCache;
        private final Map<Long, DrugInventory> inventoryCache;
        private final int totalAmount;

        DrugValidationResult(Map<Long, Drug> drugCache, Map<Long, DrugInventory> inventoryCache, int totalAmount) {
            this.drugCache = drugCache;
            this.inventoryCache = inventoryCache;
            this.totalAmount = totalAmount;
        }
    }

    /**
     * 处方+订单创建结果
     */
    private static class PrescriptionOrderResult {
        private final Prescription prescription;
        private final Order order;

        PrescriptionOrderResult(Prescription prescription, Order order) {
            this.prescription = prescription;
            this.order = order;
        }
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
        // 状态守卫：仅待支付可置为已支付，已支付幂等跳过，其他状态拒绝
        if (rx.getStatus() != null
                && rx.getStatus() == PrescriptionStatus.PAID.getCode()) {
            log.info("[prescription-paid] orderId={} already paid, skip", orderId);
            return;
        }
        if (rx.getStatus() == null
                || rx.getStatus() != PrescriptionStatus.PENDING_PAYMENT.getCode()) {
            throw new BizException(BizErrorCode.PRESCRIPTION_ALREADY_DISPENSED,
                    "处方状态非待支付，无法标记已支付");
        }
        rx.setStatus(PrescriptionStatus.PAID.getCode());
        prescriptionService.updateById(rx);

        // S28: 删除 from=to 的冗余挂号状态日志（无状态变化），改为 info 日志即可
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

        // 释放锁定库存：locked -= q, available += q
        List<PrescriptionItem> items = prescriptionItemService.list(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescriptionId));
        // 批量查库存，消除 N+1
        List<Long> drugIds = items.stream().map(PrescriptionItem::getDrugId).distinct().collect(Collectors.toList());
        Map<Long, DrugInventory> inventoryMap = drugInventoryMapper.selectList(
                new LambdaQueryWrapper<DrugInventory>().in(DrugInventory::getDrugId, drugIds))
                .stream().collect(Collectors.toMap(DrugInventory::getDrugId, inv -> inv, (i1, i2) -> i1));
        for (PrescriptionItem item : items) {
            DrugInventory inv = inventoryMap.get(item.getDrugId());
            if (inv == null) {
                continue;
            }
            int qtyBefore = inv.getAvailableQuantity() == null ? 0 : inv.getAvailableQuantity();
            drugInventoryMapper.update(null,
                    new UpdateWrapper<DrugInventory>()
                            .eq("id", inv.getId())
                            .setSql("locked_quantity = GREATEST(locked_quantity - " + item.getQuantity() + ", 0)")
                            .setSql("available_quantity = available_quantity + " + item.getQuantity()));
            inv.setAvailableQuantity(qtyBefore + item.getQuantity());

            InventoryTransaction txn = new InventoryTransaction();
            txn.setId(IdUtil.getSnowflakeNextId());
            txn.setDrugId(item.getDrugId());
            txn.setWarehouseId(inv.getWarehouseId());
            txn.setTransactionType(TXN_UNLOCK);
            txn.setRelatedOrder(String.valueOf(rx.getOrderId()));
            txn.setQuantityChange(-item.getQuantity());
            txn.setQuantityBefore(qtyBefore);
            txn.setQuantityAfter(qtyBefore + item.getQuantity());
            txn.setOperatorId(doctorId);
            txn.setOperatorName("doctor");
            inventoryTransactionService.insertInventoryTransaction(txn);
        }

        // 关闭订单
        if (rx.getOrderId() != null) {
            Order order = orderService.getOrderById(rx.getOrderId());
            if (order != null) {
                Integer fromStatus = order.getStatus();
                order.setStatus(OrderStatus.CANCELED.getCode());
                orderService.updateOrderById(order);
                // S29: 写订单状态变更日志
                OrderStatusLog orderLog = new OrderStatusLog();
                orderLog.setOrderId(order.getId());
                orderLog.setFromStatus(fromStatus);
                orderLog.setToStatus(OrderStatus.CANCELED.getCode());
                orderLog.setOperatorId(doctorId);
                orderLog.setOperatorRole("doctor");
                orderLog.setRemark("作废处方关闭订单");
                orderStatusLogManager.addOrderStatusLog(orderLog);
            }
        }

        // 处方置为已取消
        rx.setStatus(PrescriptionStatus.CANCELLED.getCode());
        prescriptionService.updateById(rx);

        // registration 状态回退：仅旧流程产生的 PENDING_PAYMENT 回退到就诊中；
        // 新流程提交后挂号已完成，作废处方不再回退挂号状态
        if (record != null) {
            Registration reg = registrationService.getRegistrationById(record.getRegistrationId());
            if (reg != null && reg.getStatus() == RegistrationStatusEnum.PENDING_PAYMENT.getCode()) {
                statusLogManager.transition(reg,
                        RegistrationStatusEnum.IN_TREATMENT.getCode(),
                        doctorId, "doctor", "作废处方");
            }
        }
    }

    /**
     * 医生端 - 处方列表（按当前医生过滤）
     * @param doctorId 医生ID
     * @return 处方列表 VO
     */
    public List<DoctorPrescriptionListVO> listDoctorPrescriptions(Long doctorId) {
        if (doctorId == null) {
            return Collections.emptyList();
        }
        List<MedicalRecord> records = medicalRecordService.list(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getDoctorId, doctorId));
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, MedicalRecord> recordMap = records.stream()
                .collect(Collectors.toMap(MedicalRecord::getId, r -> r));
        List<Long> recordIds = records.stream()
                .map(MedicalRecord::getId)
                .collect(Collectors.toList());

        List<Prescription> prescriptions = prescriptionService.list(
                new LambdaQueryWrapper<Prescription>()
                        .in(Prescription::getMedicalRecordId, recordIds)
                        .eq(Prescription::getDeleted, false)
                        .orderByDesc(Prescription::getCreateTime));
        if (prescriptions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> patientIds = records.stream()
                .map(MedicalRecord::getPatientId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userService.listUsersByUserIds(patientIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        List<Long> prescriptionIds = prescriptions.stream()
                .map(Prescription::getId)
                .collect(Collectors.toList());
        Map<Long, Long> itemCountMap = prescriptionItemService.list(
                        new LambdaQueryWrapper<PrescriptionItem>()
                                .in(PrescriptionItem::getPrescriptionId, prescriptionIds))
                .stream()
                .collect(Collectors.groupingBy(PrescriptionItem::getPrescriptionId, Collectors.counting()));

        return prescriptions.stream().map(rx -> {
            DoctorPrescriptionListVO vo = new DoctorPrescriptionListVO();
            vo.setId(rx.getId());
            vo.setMedicalRecordId(rx.getMedicalRecordId());
            MedicalRecord record = recordMap.get(rx.getMedicalRecordId());
            if (record != null) {
                vo.setPatientId(record.getPatientId());
                User user = userMap.get(record.getPatientId());
                if (user != null) {
                    vo.setPatientName(user.getNickname());
                }
                Account account = accountService.getAccountByUserId(record.getPatientId());
                if (account != null) {
                    vo.setPatientPhone(account.getPhone());
                }
            }
            vo.setTotalAmount(rx.getTotalAmount());
            vo.setStatus(rx.getStatus());
            vo.setItemCount(itemCountMap.getOrDefault(rx.getId(), 0L).intValue());
            vo.setCreateTime(rx.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 医生端 - 处方详情（含医生所有权校验）
     * @param prescriptionId 处方ID
     * @param doctorId 当前医生ID
     * @return 处方详情 VO
     * @throws BizException PRESCRIPTION_NOT_FOUND / PRESCRIPTION_NOT_OWNED
     */
    public DoctorPrescriptionDetailVO getDoctorPrescriptionDetail(Long prescriptionId, Long doctorId) {
        Prescription rx = prescriptionService.getById(prescriptionId);
        if (rx == null) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        MedicalRecord record = rx.getMedicalRecordId() == null
                ? null : medicalRecordService.getById(rx.getMedicalRecordId());
        if (record == null || !doctorId.equals(record.getDoctorId())) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_OWNED);
        }

        User user = record.getPatientId() == null
                ? null : userService.getUserById(record.getPatientId());

        List<PrescriptionItem> items = prescriptionItemService.list(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescriptionId));
        List<Long> drugIds = items.stream()
                .map(PrescriptionItem::getDrugId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Drug> drugMap = drugService.listDrugsByIds(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, d -> d));

        DoctorPrescriptionDetailVO vo = new DoctorPrescriptionDetailVO();
        vo.setId(rx.getId());
        vo.setMedicalRecordId(rx.getMedicalRecordId());
        vo.setPatientId(record.getPatientId());
        if (user != null) {
            vo.setPatientName(user.getNickname());
        }
        Account account = record.getPatientId() == null
                ? null : accountService.getAccountByUserId(record.getPatientId());
        if (account != null) {
            vo.setPatientPhone(account.getPhone());
        }
        vo.setStatus(rx.getStatus());
        vo.setTotalAmount(rx.getTotalAmount());
        vo.setOrderId(rx.getOrderId());
        vo.setCreateTime(rx.getCreateTime());
        vo.setItems(items.stream().map(item -> {
            DoctorPrescriptionDetailVO.PrescriptionItemVO itemVO = new DoctorPrescriptionDetailVO.PrescriptionItemVO();
            itemVO.setDrugId(item.getDrugId());
            Drug drug = drugMap.get(item.getDrugId());
            if (drug != null) {
                itemVO.setCommonName(drug.getCommonName());
                itemVO.setSpecification(drug.getSpecification());
                itemVO.setUnit(drug.getUnit());
            }
            itemVO.setUnitPrice(item.getUnitPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUsageMethod(item.getUsageMethod());
            return itemVO;
        }).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 用户端 - 处方列表（按就诊人过滤）
     * ponytail: N+1 查询，用户处方列表 < 100，可接受
     * @param patientUserIds 可访问的用户ID列表
     * @return 处方列表 VO
     */
    public List<PrescriptionListVO> listUserPrescriptions(List<Long> patientUserIds) {
        if (patientUserIds == null || patientUserIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<MedicalRecord> records = medicalRecordService.list(
                new LambdaQueryWrapper<MedicalRecord>()
                        .in(MedicalRecord::getPatientId, patientUserIds));
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> recordIds = records.stream()
                .map(MedicalRecord::getId)
                .collect(Collectors.toList());

        List<Prescription> prescriptions = prescriptionService.list(
                new LambdaQueryWrapper<Prescription>()
                        .in(Prescription::getMedicalRecordId, recordIds)
                        .eq(Prescription::getDeleted, false)
                        .orderByDesc(Prescription::getCreateTime));
        if (prescriptions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> prescriptionIds = prescriptions.stream()
                .map(Prescription::getId)
                .collect(Collectors.toList());
        Map<Long, Long> itemCountMap = prescriptionItemService.list(
                        new LambdaQueryWrapper<PrescriptionItem>()
                                .in(PrescriptionItem::getPrescriptionId, prescriptionIds))
                .stream()
                .collect(Collectors.groupingBy(PrescriptionItem::getPrescriptionId, Collectors.counting()));

        return prescriptions.stream().map(rx -> {
            PrescriptionListVO vo = new PrescriptionListVO();
            vo.setId(rx.getId());
            vo.setMedicalRecordId(rx.getMedicalRecordId());
            vo.setTotalAmount(rx.getTotalAmount());
            vo.setStatus(rx.getStatus());
            vo.setItemCount(itemCountMap.getOrDefault(rx.getId(), 0L).intValue());
            vo.setCreateTime(rx.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 用户端 - 处方详情（含权限校验）
     * @param prescriptionId 处方ID
     * @param userId 当前用户ID
     * @return 处方详情 VO
     * @throws BizException PRESCRIPTION_NOT_FOUND / PRESCRIPTION_NOT_OWNED
     */
    public PrescriptionDetailVO getPrescriptionDetail(Long prescriptionId, Long userId) {
        Prescription rx = prescriptionService.getById(prescriptionId);
        if (rx == null) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        // 通过 medicalRecord → patientId 校验所有权（复用可访问患者集合，含家属授权）
        MedicalRecord record = null;
        if (rx.getMedicalRecordId() != null) {
            record = medicalRecordService.getById(rx.getMedicalRecordId());
            if (record == null) {
                throw new BizException(BizErrorCode.PRESCRIPTION_NOT_OWNED);
            }
            List<Long> accessibleUserIds = patientManager.getAccessiblePatientUserIds(userId, null);
            if (!accessibleUserIds.contains(record.getPatientId())) {
                throw new BizException(BizErrorCode.PRESCRIPTION_NOT_OWNED);
            }
        }
        List<PrescriptionItem> items = prescriptionItemService.list(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescriptionId));
        List<Long> drugIds = items.stream()
                .map(PrescriptionItem::getDrugId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Drug> drugMap = drugService.listDrugsByIds(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, d -> d));

        PrescriptionDetailVO vo = new PrescriptionDetailVO();
        vo.setId(rx.getId());
        vo.setMedicalRecordId(rx.getMedicalRecordId());
        vo.setStatus(rx.getStatus());
        vo.setTotalAmount(rx.getTotalAmount());
        vo.setOrderId(rx.getOrderId());
        vo.setCreateTime(rx.getCreateTime());
        if (record != null) {
            User user = record.getPatientId() == null
                    ? null : userService.getUserById(record.getPatientId());
            if (user != null) {
                vo.setPatientName(user.getNickname());
            }
            Doctor doctor = record.getDoctorId() == null
                    ? null : doctorService.getDoctorById(record.getDoctorId());
            if (doctor != null) {
                vo.setDoctorName(doctor.getName());
                if (doctor.getDepartmentId() != null) {
                    Department dept = departmentService.getDepartmentById(doctor.getDepartmentId());
                    if (dept != null) {
                        vo.setDepartmentName(dept.getName());
                    }
                }
            }
        }
        vo.setItems(items.stream().map(item -> {
            PrescriptionDetailVO.PrescriptionItemVO itemVO = new PrescriptionDetailVO.PrescriptionItemVO();
            itemVO.setDrugId(item.getDrugId());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUsageMethod(item.getUsageMethod());
            itemVO.setUnitPrice(item.getUnitPrice());
            Drug drug = drugMap.get(item.getDrugId());
            if (drug != null) {
                itemVO.setDrugName(drug.getCommonName());
                itemVO.setSpecification(drug.getSpecification());
                itemVO.setUnit(drug.getUnit());
            }
            return itemVO;
        }).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 管理端/医生端 - 处方历史分页查询
     * @param request 查询条件
     * @param doctorId 医生ID；null 表示查询全部（管理员/药师），非null则按医生过滤
     * @return 处方分页列表
     */
    public PageInfo<com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionPageItemVO> pagePrescriptions(
            PrescriptionQueryRequest request, Long doctorId) {

        List<Long> allowedMedicalRecordIds = resolveAllowedMedicalRecordIds(doctorId, request.getPatientName());
        boolean restrictByMedicalRecord = allowedMedicalRecordIds != null;
        if (restrictByMedicalRecord && allowedMedicalRecordIds.isEmpty()) {
            return new PageInfo<>(Collections.emptyList());
        }

        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getDeleted, false)
                .orderByDesc(Prescription::getCreateTime);

        if (restrictByMedicalRecord) {
            wrapper.in(Prescription::getMedicalRecordId, allowedMedicalRecordIds);
        }
        if (request.getStatus() != null) {
            wrapper.eq(Prescription::getStatus, request.getStatus());
        }
        if (request.getStartDate() != null) {
            wrapper.ge(Prescription::getCreateTime, request.getStartDate().atStartOfDay());
        }
        if (request.getEndDate() != null) {
            wrapper.le(Prescription::getCreateTime, request.getEndDate().atTime(LocalTime.MAX));
        }

        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        List<Prescription> prescriptions = prescriptionService.list(wrapper);
        return new PageInfo<>(toAdminPageItemVOs(prescriptions));
    }

    /**
     * 根据医生ID和患者姓名解析允许查询的病历ID集合
     * @param doctorId 医生ID
     * @param patientName 患者姓名
     * @return 允许的病历ID集合；null 表示无限制
     */
    private List<Long> resolveAllowedMedicalRecordIds(Long doctorId, String patientName) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getDeleted, false);

        boolean restricted = false;
        if (doctorId != null) {
            wrapper.eq(MedicalRecord::getDoctorId, doctorId);
            restricted = true;
        }

        List<Long> patientUserIds = null;
        if (patientName != null && !patientName.trim().isEmpty()) {
            patientUserIds = userService.listUserIdsByNicknameLike(patientName.trim());
            if (patientUserIds.isEmpty()) {
                return Collections.emptyList();
            }
            wrapper.in(MedicalRecord::getPatientId, patientUserIds);
            restricted = true;
        }

        if (!restricted) {
            return null;
        }
        return medicalRecordService.list(wrapper).stream()
                .map(MedicalRecord::getId)
                .collect(Collectors.toList());
    }

    /**
     * 批量转换处方实体为管理端列表 VO
     * @param prescriptions 处方列表
     * @return 管理端列表 VO
     */
    private List<com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionPageItemVO> toAdminPageItemVOs(
            List<Prescription> prescriptions) {
        if (prescriptions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> medicalRecordIds = prescriptions.stream()
                .map(Prescription::getMedicalRecordId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, MedicalRecord> recordMap = medicalRecordService.list(
                        new LambdaQueryWrapper<MedicalRecord>().in(MedicalRecord::getId, medicalRecordIds))
                .stream()
                .collect(Collectors.toMap(MedicalRecord::getId, r -> r));

        List<Long> patientIds = recordMap.values().stream()
                .map(MedicalRecord::getPatientId)
                .distinct()
                .collect(Collectors.toList());
        List<Long> doctorIds = recordMap.values().stream()
                .map(MedicalRecord::getDoctorId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = userService.listUsersByUserIds(patientIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Doctor> doctorMap = doctorService.listDoctorsByIds(doctorIds).stream()
                .collect(Collectors.toMap(Doctor::getId, d -> d));

        List<Long> prescriptionIds = prescriptions.stream()
                .map(Prescription::getId)
                .collect(Collectors.toList());
        Map<Long, Long> itemCountMap = prescriptionItemService.list(
                        new LambdaQueryWrapper<PrescriptionItem>()
                                .in(PrescriptionItem::getPrescriptionId, prescriptionIds))
                .stream()
                .collect(Collectors.groupingBy(PrescriptionItem::getPrescriptionId, Collectors.counting()));

        return prescriptions.stream().map(rx -> {
            com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionPageItemVO vo =
                    new com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionPageItemVO();
            vo.setId(rx.getId());
            vo.setMedicalRecordId(rx.getMedicalRecordId());
            vo.setTotalAmount(rx.getTotalAmount());
            vo.setStatus(rx.getStatus());
            vo.setItemCount(itemCountMap.getOrDefault(rx.getId(), 0L).intValue());
            vo.setCreateTime(rx.getCreateTime());

            MedicalRecord record = recordMap.get(rx.getMedicalRecordId());
            if (record != null) {
                User user = userMap.get(record.getPatientId());
                if (user != null) {
                    vo.setPatientName(user.getNickname());
                }
                Doctor doctor = doctorMap.get(record.getDoctorId());
                if (doctor != null) {
                    vo.setDoctorName(doctor.getName());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 管理端 - 处方详情
     * @param id 处方ID
     * @return 处方详情 VO
     * @throws BizException PRESCRIPTION_NOT_FOUND
     */
    public com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionDetailVO getPrescriptionDetailForAdmin(Long id) {
        Prescription rx = prescriptionService.getById(id);
        if (rx == null) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        MedicalRecord record = rx.getMedicalRecordId() == null
                ? null : medicalRecordService.getById(rx.getMedicalRecordId());

        com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionDetailVO vo =
                new com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionDetailVO();
        vo.setId(rx.getId());
        vo.setMedicalRecordId(rx.getMedicalRecordId());
        vo.setStatus(rx.getStatus());
        vo.setTotalAmount(rx.getTotalAmount());
        vo.setOrderId(rx.getOrderId());
        vo.setCreateTime(rx.getCreateTime());

        if (record != null) {
            vo.setPatientId(record.getPatientId());
            User user = userService.getUserById(record.getPatientId());
            if (user != null) {
                vo.setPatientName(user.getNickname());
            }
            Account account = accountService.getAccountByUserId(record.getPatientId());
            if (account != null) {
                vo.setPatientPhone(account.getPhone());
            }
            Doctor doctor = doctorService.getDoctorById(record.getDoctorId());
            if (doctor != null) {
                vo.setDoctorName(doctor.getName());
            }
        }

        List<PrescriptionItem> items = prescriptionItemService.list(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, id));
        List<Long> drugIds = items.stream()
                .map(PrescriptionItem::getDrugId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Drug> drugMap = drugService.listDrugsByIds(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, d -> d));

        vo.setItems(items.stream().map(item -> {
            com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionDetailVO.PrescriptionItemVO itemVO =
                    new com.yirancrazy.smartmedical.pojo.dto.admin.response.PrescriptionDetailVO.PrescriptionItemVO();
            itemVO.setDrugId(item.getDrugId());
            itemVO.setUnitPrice(item.getUnitPrice());
            itemVO.setQuantity(item.getQuantity());
            itemVO.setUsageMethod(item.getUsageMethod());
            Drug drug = drugMap.get(item.getDrugId());
            if (drug != null) {
                itemVO.setCommonName(drug.getCommonName());
                itemVO.setSpecification(drug.getSpecification());
                itemVO.setUnit(drug.getUnit());
            }
            return itemVO;
        }).collect(Collectors.toList()));
        return vo;
    }
}