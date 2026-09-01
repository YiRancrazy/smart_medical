package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
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
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.pharmacy.response.DispenseVO;
import com.yirancrazy.smartmedical.pojo.dto.pharmacy.response.PendingPrescriptionVO;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import com.yirancrazy.smartmedical.service.DrugInventoryService;
import com.yirancrazy.smartmedical.service.DrugService;
import com.yirancrazy.smartmedical.service.InventoryTransactionService;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.OrderService;
import com.yirancrazy.smartmedical.service.PrescriptionItemService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final DrugInventoryMapper drugInventoryMapper;
    private final InventoryTransactionService inventoryTransactionService;
    private final RegistrationService registrationService;
    private final MedicalRecordService medicalRecordService;
    private final OrderService orderService;
    private final RegistrationStatusLogManager statusLogManager;
    private final OrderStatusLogManager orderStatusLogManager;

    /**
     * 待发药列表（status=1 已支付，F31支持可选分页）
     * @param pageNum 页码（可选）
     * @param pageSize 每页条数（可选）
     * @return 待发药处方列表（按创建时间升序）
     */
    public Result<PageResult<PendingPrescriptionVO>> listPending(Integer pageNum, Integer pageSize) {
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        List<Prescription> list = prescriptionService.list(
                new LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getStatus, PrescriptionStatus.PAID.getCode())
                        .orderByAsc(Prescription::getCreateTime));
        PageInfo<Prescription> pageInfo = new PageInfo<>(list);
        // 批量加载病历和挂号，消除 N+1
        List<Long> medicalRecordIds = list.stream()
                .map(Prescription::getMedicalRecordId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, MedicalRecord> medicalRecordMap = medicalRecordIds.isEmpty() ? Collections.emptyMap() :
                medicalRecordService.listByIds(medicalRecordIds).stream()
                        .collect(Collectors.toMap(MedicalRecord::getId, r -> r));
        List<Long> registrationIds = medicalRecordMap.values().stream()
                .map(MedicalRecord::getRegistrationId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Registration> registrationMap = registrationIds.isEmpty() ? Collections.emptyMap() :
                registrationService.listRegistrationsByIds(registrationIds).stream()
                        .collect(Collectors.toMap(Registration::getId, r -> r));

        List<PendingPrescriptionVO> vos = new ArrayList<>();
        for (Prescription rx : list) {
            PendingPrescriptionVO vo = new PendingPrescriptionVO();
            vo.setPrescriptionId(rx.getId());
            vo.setOrderId(rx.getOrderId());
            vo.setTotalAmount(rx.getTotalAmount());
            vo.setCreatedAt(rx.getCreateTime());
            if (rx.getMedicalRecordId() != null) {
                MedicalRecord rec = medicalRecordMap.get(rx.getMedicalRecordId());
                if (rec != null && rec.getRegistrationId() != null) {
                    Registration reg = registrationMap.get(rec.getRegistrationId());
                    if (reg != null) {
                        vo.setPatientId(reg.getUserId());
                        vo.setRegistrationSn(reg.getId());
                    }
                }
            }
            vos.add(vo);
        }
        return Result.success(new PageResult<>(pageInfo, vos));
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

        // 批量查药品信息，消除 N+1
        List<Long> drugIds = items.stream().map(PrescriptionItem::getDrugId).distinct().collect(Collectors.toList());
        Map<Long, Drug> drugMap = drugService.listDrugsByIds(drugIds).stream()
                .collect(Collectors.toMap(Drug::getId, d -> d));

        // 对每个药品:FOR UPDATE 行锁 + 扣减 + 流水
        for (PrescriptionItem item : items) {
            Drug drug = drugMap.get(item.getDrugId());

            // 行锁(关键:必须用自定义 selectForUpdate 方法,锁该 drugId 对应库存行)
            DrugInventory inv = drugInventoryService.selectForUpdate(item.getDrugId());
            if (inv == null) {
                throw new BizException(BizErrorCode.DRUG_NOT_FOUND,
                        "drugId=" + item.getDrugId());
            }
            // 锁定时已扣减 available，发药阶段仅校验 locked
            if (inv.getLockedQuantity() < item.getQuantity()) {
                throw new BizException(BizErrorCode.DRUG_INVENTORY_INSUFFICIENT,
                        "drugName=" + (drug == null ? item.getDrugId() : drug.getCommonName())
                                + ", locked=" + inv.getLockedQuantity()
                                + ", required=" + item.getQuantity());
            }

            int beforeStock = inv.getStockQuantity();
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
            // S30: 记 stock_quantity 前后值（发药只改 stock/locked，available 不变）
            txn.setQuantityBefore(beforeStock);
            txn.setQuantityAfter(inv.getStockQuantity());
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
                if (reg != null && !Integer.valueOf(RegistrationStatusEnum.COMPLETED.getCode()).equals(reg.getStatus())) {
                    statusLogManager.transition(reg,
                            RegistrationStatusEnum.COMPLETED.getCode(),
                            pharmacistId, "pharmacist", "发药完成");
                }
            }
        }

        vo.setPrescriptionStatus(PrescriptionStatus.DISPENSED.getCode());
        vo.setDispensedAt(dispensedAt);

        // 药品订单置为已完成
        if (rx.getOrderId() != null) {
            Order order = orderService.getOrderById(rx.getOrderId());
            if (order != null && order.getStatus() != null
                    && order.getStatus() == OrderStatus.PAID.getCode()) {
                order.setStatus(OrderStatus.FINISHED.getCode());
                orderService.updateOrderById(order);
                // S31: 写订单状态变更日志 PAID → FINISHED
                OrderStatusLog orderLog = new OrderStatusLog();
                orderLog.setOrderId(order.getId());
                orderLog.setFromStatus(OrderStatus.PAID.getCode());
                orderLog.setToStatus(OrderStatus.FINISHED.getCode());
                orderLog.setOperatorId(pharmacistId);
                orderLog.setOperatorRole("pharmacist");
                orderLog.setRemark("发药完成");
                orderStatusLogManager.addOrderStatusLog(orderLog);
            }
        }

        log.info("[pharmacy-dispense] prescriptionId={}, pharmacistId={}, itemCount={}",
                prescriptionId, pharmacistId, items.size());
        return vo;
    }

    /**
     * 库存预警列表（stock_quantity < min_stock，F31支持可选分页）
     * @param pageNum 页码（可选）
     * @param pageSize 每页条数（可选）
     * @return 库存低于最低预警线的药品库存列表,按缺口升序
     */
    public Result<PageResult<DrugInventory>> listLowStock(Integer pageNum, Integer pageSize) {
        if (pageNum != null && pageSize != null) {
            PageHelper.startPage(pageNum, pageSize);
        }
        // F31: 过滤+排序下推到 SQL，避免 Java 端分页漏数据
        // ponytail: DrugInventoryService 无 list(wrapper) 方法，直接走 mapper（同 RegistrationManager 直接注入 Mapper 的模式）
        List<DrugInventory> list = drugInventoryMapper.selectList(
                new LambdaQueryWrapper<DrugInventory>()
                        .apply("stock_quantity < min_stock")
                        .last("ORDER BY (stock_quantity - min_stock) ASC"));
        PageInfo<DrugInventory> pageInfo = new PageInfo<>(list);
        return Result.success(new PageResult<>(pageInfo, list));
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

    /**
     * 药师端 - 处方详情（仅查询）
     * @param prescriptionId 处方ID
     * @return 处方实体
     */
    public Prescription getPrescriptionById(Long prescriptionId) {
        return prescriptionService.getById(prescriptionId);
    }
}