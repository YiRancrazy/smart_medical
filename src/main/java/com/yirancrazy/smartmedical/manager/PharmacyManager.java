package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Drug;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.dto.pharmacy.response.DispenseVO;
import com.yirancrazy.smartmedical.pojo.dto.pharmacy.response.PendingPrescriptionVO;
import com.yirancrazy.smartmedical.service.DrugInventoryService;
import com.yirancrazy.smartmedical.service.DrugService;
import com.yirancrazy.smartmedical.service.InventoryTransactionService;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.PrescriptionItemService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 药师业务编排
 * @Author: YiRanCrazy@gmail.com
 * @Description: 发药大事务(扣库存+行锁+流水+状态机) + 待发药列表 + 库存预警
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class PharmacyManager {

    /** 库存异动类型:入库 */
    private static final int TXN_INBOUND = 1;
    /** 库存异动类型:出库 */
    private static final int TXN_OUTBOUND = 2;
    /** 库存异动类型:盘点调整 */
    private static final int TXN_ADJUST = 3;

    private final PrescriptionService prescriptionService;
    private final PrescriptionItemService prescriptionItemService;
    private final DrugService drugService;
    private final DrugInventoryService drugInventoryService;
    private final InventoryTransactionService inventoryTransactionService;
    private final RegistrationService registrationService;
    private final MedicalRecordService medicalRecordService;
    private final RegistrationStatusLogManager statusLogManager;

    /**
     * 待发药列表（status=1 已支付）
     * @return 待发药处方列表（按创建时间升序）
     */
    public List<PendingPrescriptionVO> listPending() {
        List<Prescription> list = prescriptionService.list(
                new LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getStatus, PrescriptionStatus.PAID.getCode())
                        .orderByAsc(Prescription::getCreateTime));
        List<PendingPrescriptionVO> vos = new ArrayList<>();
        for (Prescription rx : list) {
            PendingPrescriptionVO vo = new PendingPrescriptionVO();
            vo.setPrescriptionId(rx.getId());
            vo.setOrderId(rx.getOrderId());
            vo.setTotalAmount(rx.getTotalAmount());
            vo.setCreatedAt(rx.getCreateTime());
            if (rx.getMedicalRecordId() != null) {
                MedicalRecord rec = medicalRecordService.getById(rx.getMedicalRecordId());
                if (rec != null && rec.getRegistrationId() != null) {
                    Registration reg = registrationService.getRegistrationById(rec.getRegistrationId());
                    if (reg != null) {
                        vo.setPatientId(reg.getUserId());
                        vo.setRegistrationSn(reg.getId());
                    }
                }
            }
            vos.add(vo);
        }
        return vos;
    }

    /**
     * 扫码发药（4 表大事务 + 行级悲观锁）
     * @param prescriptionId 处方ID
     * @param pharmacistId 发药药师ID
     * @return 发药结果 VO（处方状态 + 明细）
     * @throws BizException PRESCRIPTION_NOT_FOUND / PRESCRIPTION_NOT_PAID / DRUG_NOT_FOUND / DRUG_INVENTORY_INSUFFICIENT
     */
    @Transactional(rollbackFor = Exception.class)
    public DispenseVO dispense(Long prescriptionId, Long pharmacistId) {
        Prescription rx = prescriptionService.getById(prescriptionId);
        if (rx == null) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_FOUND);
        }
        if (rx.getStatus() == null
                || rx.getStatus() != PrescriptionStatus.PAID.getCode()) {
            throw new BizException(BizErrorCode.PRESCRIPTION_NOT_PAID);
        }

        // 加载所有 item
        List<PrescriptionItem> items = prescriptionItemService.list(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, prescriptionId));

        DispenseVO vo = new DispenseVO();
        vo.setPrescriptionId(prescriptionId);
        List<DispenseVO.DispenseItem> voItems = new ArrayList<>();

        // 对每个药品:FOR UPDATE 行锁 + 扣减 + 流水
        for (PrescriptionItem item : items) {
            Drug drug = drugService.getDrugById(item.getDrugId());

            // 行锁(关键:必须用自定义 selectForUpdate 方法,锁该 drugId 对应库存行)
            DrugInventory inv = drugInventoryService.selectForUpdate(item.getDrugId());
            if (inv == null) {
                throw new BizException(BizErrorCode.DRUG_NOT_FOUND,
                        "drugId=" + item.getDrugId());
            }
            if (inv.getAvailableQuantity() < item.getQuantity()
                    || inv.getLockedQuantity() < item.getQuantity()) {
                throw new BizException(BizErrorCode.DRUG_INVENTORY_INSUFFICIENT,
                        "drugName=" + (drug == null ? item.getDrugId() : drug.getCommonName())
                                + ", available=" + inv.getAvailableQuantity()
                                + ", locked=" + inv.getLockedQuantity()
                                + ", required=" + item.getQuantity());
            }

            int beforeAvailable = inv.getAvailableQuantity();
            // 锁定时已扣减 available，发药仅扣减 stock 与 locked
            inv.setStockQuantity(inv.getStockQuantity() - item.getQuantity());
            inv.setLockedQuantity(inv.getLockedQuantity() - item.getQuantity());
            inv.setLastOutboundTime(LocalDateTime.now());
            drugInventoryService.updateDrugInventoryById(inv);

            // 出库流水
            InventoryTransaction txn = new InventoryTransaction();
            txn.setId(IdUtil.getSnowflakeNextId());
            txn.setDrugId(item.getDrugId());
            txn.setWarehouseId(inv.getWarehouseId());
            txn.setTransactionType(TXN_OUTBOUND);
            txn.setRelatedOrder(rx.getOrderId() == null ? null : String.valueOf(rx.getOrderId()));
            txn.setQuantityChange(-item.getQuantity());
            txn.setQuantityBefore(beforeAvailable);
            txn.setQuantityAfter(inv.getAvailableQuantity());
            txn.setOperatorId(pharmacistId);
            txn.setOperatorName("pharmacist");
            txn.setRemark("发药出库");
            inventoryTransactionService.insertInventoryTransaction(txn);

            DispenseVO.DispenseItem voItem = new DispenseVO.DispenseItem();
            voItem.setDrugId(item.getDrugId());
            voItem.setDrugName(drug == null ? null : drug.getCommonName());
            voItem.setQuantity(item.getQuantity());
            voItem.setStockAfter(inv.getStockQuantity());
            voItems.add(voItem);
        }
        vo.setItems(voItems);

        // 处方置为已发药
        LocalDateTime dispensedAt = LocalDateTime.now();
        rx.setStatus(PrescriptionStatus.DISPENSED.getCode());
        rx.setPharmacistId(pharmacistId);
        rx.setDispensedAt(dispensedAt);
        prescriptionService.updateById(rx);

        // registration 状态迁移 → COMPLETED(已就诊/完成),自动填充 visitEndTime
        if (rx.getMedicalRecordId() != null) {
            MedicalRecord record = medicalRecordService.getById(rx.getMedicalRecordId());
            if (record != null && record.getRegistrationId() != null) {
                Registration reg = registrationService.getRegistrationById(record.getRegistrationId());
                if (reg != null && reg.getStatus() != RegistrationStatusEnum.COMPLETED.getCode()) {
                    statusLogManager.transition(reg,
                            RegistrationStatusEnum.COMPLETED.getCode(),
                            pharmacistId, "pharmacist", "发药完成");
                }
            }
        }

        vo.setPrescriptionStatus(PrescriptionStatus.DISPENSED.getCode());
        vo.setDispensedAt(dispensedAt);
        log.info("[pharmacy-dispense] prescriptionId={}, pharmacistId={}, itemCount={}",
                prescriptionId, pharmacistId, items.size());
        return vo;
    }

    /**
     * 库存预警列表（stock_quantity < min_stock）
     * @return 库存低于最低预警线的药品库存列表,按缺口升序
     */
    public List<DrugInventory> listLowStock() {
        return drugInventoryService.listAllDrugInventories()
                .stream()
                .filter(inv -> inv.getStockQuantity() != null
                        && inv.getMinStock() != null
                        && inv.getStockQuantity() < inv.getMinStock())
                .sorted((a, b) -> Integer.compare(
                        a.getStockQuantity() - a.getMinStock(),
                        b.getStockQuantity() - b.getMinStock()))
                .toList();
    }

    /**
     * 库存入库
     * @param drugId 药品ID
     * @param quantity 入库数量（正整数）
     * @param warehouseId 仓库ID
     * @param operatorId 操作人ID
     * @return 入库后的库存
     */
    @Transactional(rollbackFor = Exception.class)
    public DrugInventory stockIn(Long drugId, Integer quantity, Long warehouseId, Long operatorId) {
        DrugInventory inv = drugInventoryService.selectForUpdate(drugId);
        if (inv == null) {
            throw new BizException(BizErrorCode.DRUG_NOT_FOUND, "drugId=" + drugId);
        }
        int beforeAvailable = inv.getAvailableQuantity();
        inv.setStockQuantity(inv.getStockQuantity() + quantity);
        inv.setAvailableQuantity(inv.getAvailableQuantity() + quantity);
        drugInventoryService.updateDrugInventoryById(inv);

        InventoryTransaction txn = new InventoryTransaction();
        txn.setId(IdUtil.getSnowflakeNextId());
        txn.setDrugId(drugId);
        txn.setWarehouseId(warehouseId);
        txn.setTransactionType(TXN_INBOUND);
        txn.setQuantityChange(quantity);
        txn.setQuantityBefore(beforeAvailable);
        txn.setQuantityAfter(inv.getAvailableQuantity());
        txn.setOperatorId(operatorId);
        txn.setOperatorName("pharmacist");
        txn.setRemark("手动入库");
        inventoryTransactionService.insertInventoryTransaction(txn);

        log.info("[pharmacy-stockIn] drugId={}, +{}, after={}", drugId, quantity, inv.getAvailableQuantity());
        return inv;
    }

    /**
     * 盘点调整（校正实际库存数量）
     * @param drugId 药品ID
     * @param actualQuantity 实际盘点数量
     * @param warehouseId 仓库ID
     * @param operatorId 操作人ID
     * @param remark 备注
     * @return 调整后的库存
     */
    @Transactional(rollbackFor = Exception.class)
    public DrugInventory stockAdjust(Long drugId, Integer actualQuantity, Long warehouseId, Long operatorId, String remark) {
        DrugInventory inv = drugInventoryService.selectForUpdate(drugId);
        if (inv == null) {
            throw new BizException(BizErrorCode.DRUG_NOT_FOUND, "drugId=" + drugId);
        }
        int beforeAvailable = inv.getAvailableQuantity();
        int change = actualQuantity - beforeAvailable;
        inv.setStockQuantity(inv.getStockQuantity() + change);
        inv.setAvailableQuantity(actualQuantity);
        drugInventoryService.updateDrugInventoryById(inv);

        InventoryTransaction txn = new InventoryTransaction();
        txn.setId(IdUtil.getSnowflakeNextId());
        txn.setDrugId(drugId);
        txn.setWarehouseId(warehouseId);
        txn.setTransactionType(TXN_ADJUST);
        txn.setQuantityChange(change);
        txn.setQuantityBefore(beforeAvailable);
        txn.setQuantityAfter(inv.getAvailableQuantity());
        txn.setOperatorId(operatorId);
        txn.setOperatorName("pharmacist");
        txn.setRemark(remark != null ? remark : "盘点调整");
        inventoryTransactionService.insertInventoryTransaction(txn);

        log.info("[pharmacy-stockAdjust] drugId={}, {}→{}", drugId, beforeAvailable, actualQuantity);
        return inv;
    }
}