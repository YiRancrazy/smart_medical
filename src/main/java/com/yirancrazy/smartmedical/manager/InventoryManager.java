package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;
import com.yirancrazy.smartmedical.service.DrugInventoryService;
import com.yirancrazy.smartmedical.service.InventoryTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 药品库存编排
 * @Author: YiRanCrazy@gmail.com
 * @Description: 库存锁定/释放与异动流水，统一走行级悲观锁，避免并发丢更新
 * @Datetime: 2026-10-01 10:00
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class InventoryManager {

    /** 库存异动类型:锁定 */
    private static final int TXN_LOCK = 4;
    /** 库存异动类型:解锁 */
    private static final int TXN_UNLOCK = 5;

    private final DrugInventoryService drugInventoryService;
    private final InventoryTransactionService inventoryTransactionService;

    /**
     * 锁定库存:行锁下增加 locked_quantity 并写锁定流水
     * 调用方必须在事务内调用，否则行锁会随语句提交立即释放
     * @param drugId 药品ID
     * @param quantity 锁定数量(正整数)
     * @param relatedOrder 关联订单号
     * @param operatorId 操作人ID
     * @param operatorName 操作人角色名
     * @return 锁定后的库存行
     * @throws BizException DRUG_NOT_FOUND / DRUG_INVENTORY_INSUFFICIENT
     */
    public DrugInventory lock(Long drugId, Integer quantity, String relatedOrder,
                              Long operatorId, String operatorName) {
        assertPositiveQuantity(quantity);
        DrugInventory inv = drugInventoryService.selectForUpdate(drugId);
        if (inv == null) {
            throw new BizException(BizErrorCode.DRUG_NOT_FOUND, "drugId=" + drugId);
        }
        int available = nvl(inv.getAvailableQuantity());
        int locked = nvl(inv.getLockedQuantity());
        if (available - locked < quantity) {
            throw new BizException(BizErrorCode.DRUG_INVENTORY_INSUFFICIENT,
                    "drugId=" + drugId + ", available=" + available
                            + ", locked=" + locked + ", required=" + quantity);
        }
        inv.setLockedQuantity(locked + quantity);
        drugInventoryService.updateDrugInventoryById(inv);
        writeTransaction(inv, TXN_LOCK, quantity, relatedOrder, operatorId, operatorName, "开方锁定库存");
        log.info("[inventory-lock] drugId={}, +{}, lockedAfter={}", drugId, quantity, inv.getLockedQuantity());
        return inv;
    }

    /**
     * 释放库存:行锁下减少 locked_quantity 并写解锁流水
     * @param drugId 药品ID
     * @param quantity 释放数量(正整数)
     * @param relatedOrder 关联订单号
     * @param operatorId 操作人ID
     * @param operatorName 操作人角色名
     * @throws BizException DRUG_NOT_FOUND / DRUG_INVENTORY_INSUFFICIENT
     */
    public void release(Long drugId, Integer quantity, String relatedOrder,
                        Long operatorId, String operatorName) {
        assertPositiveQuantity(quantity);
        DrugInventory inv = drugInventoryService.selectForUpdate(drugId);
        if (inv == null) {
            throw new BizException(BizErrorCode.DRUG_NOT_FOUND, "drugId=" + drugId);
        }
        int locked = nvl(inv.getLockedQuantity());
        if (locked < quantity) {
            throw new BizException(BizErrorCode.DRUG_INVENTORY_INSUFFICIENT,
                    "drugId=" + drugId + ", locked=" + locked + ", required=" + quantity);
        }
        inv.setLockedQuantity(locked - quantity);
        drugInventoryService.updateDrugInventoryById(inv);
        writeTransaction(inv, TXN_UNLOCK, -quantity, relatedOrder, operatorId, operatorName, "作废处方释放库存");
        log.info("[inventory-release] drugId={}, -{}, lockedAfter={}", drugId, quantity, inv.getLockedQuantity());
    }

    /**
     * 写库存异动流水，前后数量统一记录 locked_quantity
     */
    private void writeTransaction(DrugInventory inv, int type, int change, String relatedOrder,
                                  Long operatorId, String operatorName, String remark) {
        int after = nvl(inv.getLockedQuantity());
        InventoryTransaction txn = new InventoryTransaction();
        txn.setId(IdUtil.getSnowflakeNextId());
        txn.setDrugId(inv.getDrugId());
        txn.setWarehouseId(inv.getWarehouseId());
        txn.setTransactionType(type);
        txn.setRelatedOrder(relatedOrder);
        txn.setQuantityChange(change);
        txn.setQuantityBefore(after - change);
        txn.setQuantityAfter(after);
        txn.setOperatorId(operatorId);
        txn.setOperatorName(operatorName);
        txn.setRemark(remark);
        inventoryTransactionService.insertInventoryTransaction(txn);
    }

    private void assertPositiveQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new BizException(BizErrorCode.DRUG_INVENTORY_INSUFFICIENT, "quantity must be positive");
        }
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }
}
