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
}
