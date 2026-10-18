package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.DrugInventoryMapper;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.service.DrugInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品库存服务实现类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class DrugInventoryServiceImpl implements DrugInventoryService {

    private final DrugInventoryMapper drugInventoryMapper;

    @Override
    public Integer insertDrugInventory(DrugInventory drugInventory) {
        return drugInventoryMapper.insert(drugInventory);
    }

    @Override
    public DrugInventory getDrugInventoryById(Long id) {
        return drugInventoryMapper.selectById(id);
    }

    @Override
    public Integer updateDrugInventoryById(DrugInventory drugInventory) {
        return drugInventoryMapper.updateById(drugInventory);
    }

    @Override
    public Integer deleteDrugInventoryById(Long id) {
        return drugInventoryMapper.deleteById(id);
    }

    @Override
    public List<DrugInventory> listAllDrugInventories() {
        return drugInventoryMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public PageInfo<DrugInventory> listDrugInventoriesByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<DrugInventory> drugInventories = drugInventoryMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(drugInventories);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DrugInventory selectForUpdate(Long drugId) {
        return drugInventoryMapper.selectForUpdate(drugId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long countLowStock() {
        return drugInventoryMapper.selectCount(new QueryWrapper<DrugInventory>()
                .apply("available_quantity < min_stock"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DrugInventory> listLowStock() {
        return drugInventoryMapper.selectList(new LambdaQueryWrapper<DrugInventory>()
                .apply("stock_quantity < min_stock")
                .last("ORDER BY (stock_quantity - min_stock) ASC"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DrugInventory> listByDrugIds(List<Long> drugIds) {
        if (drugIds == null || drugIds.isEmpty()) {
            return List.of();
        }
        return drugInventoryMapper.selectList(new LambdaQueryWrapper<DrugInventory>()
                .in(DrugInventory::getDrugId, drugIds));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DrugInventory> listByDrugIdsForUpdate(List<Long> drugIds) {
        if (drugIds == null || drugIds.isEmpty()) {
            return List.of();
        }
        return drugInventoryMapper.selectList(new LambdaQueryWrapper<DrugInventory>()
                .in(DrugInventory::getDrugId, drugIds)
                .last("FOR UPDATE"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lockInventory(Long inventoryId, int quantity) {
        return drugInventoryMapper.update(null,
                new UpdateWrapper<DrugInventory>()
                        .eq("id", inventoryId)
                        .ge("available_quantity", quantity)
                        .setSql("locked_quantity = locked_quantity + " + quantity)
                        .setSql("available_quantity = available_quantity - " + quantity));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int releaseInventory(Long inventoryId, int quantity) {
        return drugInventoryMapper.update(null,
                new UpdateWrapper<DrugInventory>()
                        .eq("id", inventoryId)
                        .setSql("locked_quantity = locked_quantity - " + quantity)
                        .setSql("available_quantity = available_quantity + " + quantity));
    }
}
