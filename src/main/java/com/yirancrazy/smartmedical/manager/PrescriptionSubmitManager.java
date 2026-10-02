package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.OrderTypeEnum;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.constant.ProductionTypeEnum;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.constant.type.RoleEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Drug;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.PrescriptionItemRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.SubmitPrescriptionRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.PrescriptionSubmitVO;
import com.yirancrazy.smartmedical.service.DrugInventoryService;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 处方提交业务编排
 * @Author: YiRanCrazy@gmail.com
 * @Description: 提交病历、创建处方与药品订单并锁定库存
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class PrescriptionSubmitManager {

    /** 库存异动类型:锁定 */
    private static final int TXN_LOCK = 4;

    private final RegistrationService registrationService;
    private final RegistrationScheduleTemplateService registrationScheduleTemplateService;
    private final MedicalRecordService medicalRecordService;
    private final PrescriptionService prescriptionService;
    private final PrescriptionItemService prescriptionItemService;
    private final DrugService drugService;
    private final DrugInventoryService drugInventoryService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final InventoryTransactionService inventoryTransactionService;
    private final RegistrationStatusLogService registrationStatusLogService;
    private final RegistrationScheduleService registrationScheduleService;

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
        if (drugResult.totalAmount > Integer.MAX_VALUE) {
            throw new BizException(BizErrorCode.ORDER_AMOUNT_TOO_LARGE,
                    "处方金额超出上限，无法提交");
        }

        // 4. 创建处方 + 药品订单
        PrescriptionOrderResult poResult = createPrescriptionAndOrder(reg, record, drugResult.totalAmount);

        // 5. 逐条:处方明细 + 订单明细 + 锁定库存 + 流水
        processDrugItems(req.getItems(), poResult.prescription, poResult.order, doctorId,
                drugResult.drugCache, drugResult.inventoryCache);

        // 6. registration 状态迁移:就诊中 → 完成
        Integer fromStatus = reg.getStatus();
        int toStatus = RegistrationStatusEnum.COMPLETED.getCode();
        if (!registrationService.updateStatusIfCurrent(reg, toStatus)) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID, "状态已变更，请刷新");
        }
        registrationStatusLogService.writeLog(reg.getId(), fromStatus, toStatus,
                doctorId, RoleEnum.DOCTOR.getRole(), "提交病历开方");

        // 7. 构造返回 VO
        PrescriptionSubmitVO vo = new PrescriptionSubmitVO();
        vo.setMedicalRecordId(record.getId());
        vo.setPrescriptionId(poResult.prescription.getId());
        vo.setOrderId(poResult.order.getId());
        vo.setOrderSn(String.valueOf(poResult.order.getSn()));
        vo.setTotalAmount((int) drugResult.totalAmount);
        vo.setRegistrationStatus(RegistrationStatusEnum.COMPLETED.getCode());
        return vo;
    }

    /**
     * 校验挂号记录存在性、医生归属和状态
     */
    private Registration validateRegistration(Long regId, Long doctorId) {
        Registration reg = registrationService.getRegistrationById(regId);
        if (reg == null) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_FOUND);
        }
        RegistrationSchedule schedule = registrationScheduleService
                .getRegistrationScheduleById(reg.getRegistrationScheduleId());
        RegistrationScheduleTemplate template = schedule == null ? null
                : registrationScheduleTemplateService
                .getRegistrationScheduleTemplateById(schedule.getRegistrationScheduleTemplateId());
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
        long totalAmount = 0L;
        List<Long> drugIds = items.stream().map(PrescriptionItemRequest::getDrugId).distinct().collect(Collectors.toList());
        Map<Long, Drug> drugCache = drugService.listDrugsByIds(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, d -> d));
        Map<Long, DrugInventory> inventoryCache = drugInventoryService.listByDrugIds(drugIds)
                .stream().collect(Collectors.toMap(DrugInventory::getDrugId, inv -> inv, (i1, i2) -> i1));
        for (PrescriptionItemRequest item : items) {
            Drug drug = drugCache.get(item.getDrugId());
            if (drug == null) {
                throw new BizException(BizErrorCode.DRUG_NOT_FOUND, "drugId=" + item.getDrugId());
            }
            if (drug.getPrice() == null) {
                throw new BizException(BizErrorCode.DRUG_PRICE_INVALID, "drugId=" + item.getDrugId());
            }
            DrugInventory inv = inventoryCache.get(item.getDrugId());
            if (inv == null || inv.getAvailableQuantity() < item.getQuantity()) {
                throw new BizException(BizErrorCode.DRUG_INVENTORY_INSUFFICIENT,
                        "drugName=" + drug.getCommonName()
                                + ", available=" + (inv == null ? 0 : inv.getAvailableQuantity())
                                + ", required=" + item.getQuantity());
            }
            totalAmount += (long) drug.getPrice() * item.getQuantity();
        }
        return new DrugValidationResult(drugCache, inventoryCache, totalAmount);
    }

    /**
     * 创建或更新病历记录
     */
    private MedicalRecord saveMedicalRecord(Registration reg, Long doctorId, SubmitPrescriptionRequest req) {
        Long regId = reg.getId();
        MedicalRecord record = medicalRecordService.getByRegistrationId(regId);
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
        Integer fromStatus = reg.getStatus();
        int toStatus = RegistrationStatusEnum.COMPLETED.getCode();
        if (!registrationService.updateStatusIfCurrent(reg, toStatus)) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID, "状态已变更，请刷新");
        }
        registrationStatusLogService.writeLog(reg.getId(), fromStatus, toStatus,
                doctorId, RoleEnum.DOCTOR.getRole(), "就诊完成(无处方)");
        PrescriptionSubmitVO vo = new PrescriptionSubmitVO();
        vo.setMedicalRecordId(record.getId());
        vo.setTotalAmount(0);
        vo.setRegistrationStatus(RegistrationStatusEnum.COMPLETED.getCode());
        return vo;
    }

    /**
     * 创建处方头 + 药品订单，并关联订单ID到处方
     */
    private PrescriptionOrderResult createPrescriptionAndOrder(Registration reg, MedicalRecord record, long totalAmount) {
        int amount = (int) totalAmount;
        Prescription rx = new Prescription();
        rx.setId(IdUtil.getSnowflakeNextId());
        rx.setMedicalRecordId(record.getId());
        rx.setTotalAmount(amount);
        rx.setStatus(PrescriptionStatus.PENDING_PAYMENT.getCode());
        prescriptionService.save(rx);

        Order order = new Order();
        order.setId(IdUtil.getSnowflakeNextId());
        order.setUserId(reg.getUserId());
        order.setOrderTypeId(OrderTypeEnum.DRUG.getCode());
        order.setSn(IdUtil.getSnowflakeNextId());
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());
        order.setTotalAmount(amount);
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
            orderItem.setProductionTypeId(ProductionTypeEnum.DRUG.getCode());
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
            int rows = drugInventoryService.lockInventory(inv.getId(), item.getQuantity());
            if (rows == 0) {
                throw new BizException(BizErrorCode.DRUG_INVENTORY_INSUFFICIENT,
                        "库存锁定失败(并发)：drugId=" + item.getDrugId());
            }
            // P6: 审计流水取 DB 真实值（事务内可见自身更新），避免并发下快照偏差
            DrugInventory fresh = drugInventoryService.getDrugInventoryById(inv.getId());
            int qtyAfter = fresh == null || fresh.getAvailableQuantity() == null
                    ? qtyBefore - item.getQuantity() : fresh.getAvailableQuantity();
            int qtyBeforeReal = qtyAfter + item.getQuantity();
            inv.setLockedQuantity((inv.getLockedQuantity() == null ? 0 : inv.getLockedQuantity()) + item.getQuantity());
            inv.setAvailableQuantity(qtyAfter);

            InventoryTransaction txn = new InventoryTransaction();
            txn.setId(IdUtil.getSnowflakeNextId());
            txn.setDrugId(item.getDrugId());
            txn.setWarehouseId(inv.getWarehouseId());
            txn.setTransactionType(TXN_LOCK);
            txn.setRelatedOrder(String.valueOf(order.getSn()));
            txn.setQuantityChange(item.getQuantity());
            txn.setQuantityBefore(qtyBeforeReal);
            txn.setQuantityAfter(qtyAfter);
            txn.setOperatorId(doctorId);
            txn.setOperatorName(RoleEnum.DOCTOR.getRole());
            inventoryTransactionService.insertInventoryTransaction(txn);
        }
    }

    /**
     * 药品校验结果：药品缓存、库存缓存、总金额
     */
    private static class DrugValidationResult {
        private final Map<Long, Drug> drugCache;
        private final Map<Long, DrugInventory> inventoryCache;
        private final long totalAmount;

        DrugValidationResult(Map<Long, Drug> drugCache, Map<Long, DrugInventory> inventoryCache, long totalAmount) {
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
}
