package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
}
