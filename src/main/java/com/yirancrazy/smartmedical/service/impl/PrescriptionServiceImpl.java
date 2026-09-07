package com.yirancrazy.smartmedical.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.PrescriptionMapper;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.PrescriptionItem;
import com.yirancrazy.smartmedical.service.DrugInventoryService;
import com.yirancrazy.smartmedical.service.InventoryTransactionService;
import com.yirancrazy.smartmedical.service.PrescriptionItemService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 处方 Service 实现
 * @Author: YiRanCrazy@gmail.com
 * @Description: 处方 Service 实现
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl
        extends ServiceImpl<PrescriptionMapper, Prescription>
        implements PrescriptionService {

    /** 库存异动类型:解锁 */
    private static final int TXN_UNLOCK = 5;

    private final PrescriptionItemService prescriptionItemService;
    private final DrugInventoryService drugInventoryService;
    private final InventoryTransactionService inventoryTransactionService;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsPaid(Long orderId) {
        Prescription rx = getOne(
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
        updateById(rx);

        log.info("[prescription-paid] prescriptionId={}, orderId={}", rx.getId(), orderId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Prescription getByOrderId(Long orderId) {
        return getOne(new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getOrderId, orderId)
                .last("LIMIT 1"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelExpiredPendingPrescription(Long prescriptionId, String remark) {
        Prescription rx = getById(prescriptionId);
        if (rx == null) {
            log.warn("[prescription-timeout] prescriptionId={} 不存在，跳过", prescriptionId);
            return false;
        }
        // 状态守卫：仅待支付可置为已取消；已被支付/取消的处方不处理，返回 false 由调用方跳过
        if (rx.getStatus() == null
                || rx.getStatus() != PrescriptionStatus.PENDING_PAYMENT.getCode()) {
            log.info("[prescription-timeout] prescriptionId={} 状态={}，非待支付跳过",
                    prescriptionId, rx.getStatus());
            return false;
        }
        boolean rxUpdated = update(new UpdateWrapper<Prescription>()
                .eq("id", rx.getId())
                .eq("status", PrescriptionStatus.PENDING_PAYMENT.getCode())
                .set("status", PrescriptionStatus.CANCELLED.getCode()));
        if (!rxUpdated) {
            log.info("[prescription-timeout] prescriptionId={} 并发状态已变化，跳过", prescriptionId);
            return false;
        }
        log.info("[prescription-timeout] 处方 prescriptionId={} 已作废（{}）", prescriptionId, remark);
        // 释放锁定库存并写解锁流水（行锁防并发双释放；先作废处方再解锁，作废失败不触碰库存）
        releaseLockedStock(rx, 0L, "system", remark);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseLockedStock(Prescription rx, Long operatorId, String operatorName, String remark) {
        List<PrescriptionItem> items = prescriptionItemService.list(
                new LambdaQueryWrapper<PrescriptionItem>()
                        .eq(PrescriptionItem::getPrescriptionId, rx.getId()));
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
