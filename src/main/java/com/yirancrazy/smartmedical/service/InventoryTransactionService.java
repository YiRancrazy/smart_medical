package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 库存异动流水服务接口
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

public interface InventoryTransactionService {

    /**
     * 添加库存异动流水记录
     * @param inventoryTransaction 库存异动流水对象
     * @return 添加结果
     */
    Integer insertInventoryTransaction(InventoryTransaction inventoryTransaction);

    /**
     * 根据ID查询库存异动流水记录
     * @param id 记录ID
     * @return 库存异动流水对象
     */
    InventoryTransaction getInventoryTransactionById(Long id);

    /**
     * 根据ID更新库存异动流水记录
     * @param inventoryTransaction 库存异动流水对象
     * @return 更新结果
     */
    Integer updateInventoryTransactionById(InventoryTransaction inventoryTransaction);

    /**
     * 根据ID删除库存异动流水记录
     * @param id 记录ID
     * @return 删除结果
     */
    Integer deleteInventoryTransactionById(Long id);

    /**
     * 查询所有库存异动流水记录列表
     * @return 库存异动流水记录列表
     */
    List<InventoryTransaction> listAllInventoryTransactions();

    /**
     * 分页查询库存异动流水记录列表
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<InventoryTransaction> listInventoryTransactionsByPage(Integer pageNum, Integer pageSize);
}
