package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.DrugInventory;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品库存服务接口
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

public interface DrugInventoryService {

    /**
     * 插入药品库存
     * @param drugInventory 要插入的药品库存
     * @return 插入的行数
     */
    Integer insertDrugInventory(DrugInventory drugInventory);

    /**
     * 根据ID查询药品库存
     * @param id 要查询的ID
     * @return 查询到的药品库存
     */
    DrugInventory getDrugInventoryById(Long id);

    /**
     * 更新药品库存
     * @param drugInventory 要更新的药品库存
     * @return 更新的行数
     */
    Integer updateDrugInventoryById(DrugInventory drugInventory);

    /**
     * 删除药品库存
     * @param id 要删除的ID
     * @return 删除的行数
     */
    Integer deleteDrugInventoryById(Long id);

    /**
     * 查询所有药品库存
     * @return 所有药品库存列表
     */
    List<DrugInventory> listAllDrugInventories();

    /**
     * 分页查询药品库存
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页的药品库存列表
     */
    PageInfo<DrugInventory> listDrugInventoriesByPage(Integer pageNum, Integer pageSize);

    /**
     * 行级悲观锁查询(SELECT ... FOR UPDATE)，用于发药大事务中按 drugId 锁库存行
     * @param drugId 药品ID
     * @return 库存记录(已加排他锁，需在事务内调用)
     */
    DrugInventory selectForUpdate(Long drugId);

    /**
     * 统计库存预警数量（available_quantity < min_stock）
     * @return 预警库存条数
     */
    Long countLowStock();

    /**
     * 库存预警列表（stock_quantity < min_stock，按缺口升序）
     * @return 预警库存列表
     */
    List<DrugInventory> listLowStock();

    /**
     * 按药品ID集合批量查询库存
     * @param drugIds 药品ID集合
     * @return 库存列表
     */
    List<DrugInventory> listByDrugIds(List<Long> drugIds);

    /**
     * 按药品ID集合批量查询库存（FOR UPDATE 行锁，需在事务内调用，防并发释放）
     * @param drugIds 药品ID集合
     * @return 库存列表
     */
    List<DrugInventory> listByDrugIdsForUpdate(List<Long> drugIds);

    /**
     * 原子锁定库存：WHERE available_quantity >= qty 时 locked+q、available-q（防并发超锁）
     * @param inventoryId 库存ID
     * @param quantity 锁定数量
     * @return 影响行数（0 表示库存不足，锁定失败）
     */
    int lockInventory(Long inventoryId, int quantity);

    /**
     * 原子释放库存：locked-q、available+q
     * @param inventoryId 库存ID
     * @param quantity 释放数量
     * @return 影响行数
     */
    int releaseInventory(Long inventoryId, int quantity);
}
