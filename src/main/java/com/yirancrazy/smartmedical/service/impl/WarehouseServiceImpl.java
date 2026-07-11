package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.WarehouseMapper;
import com.yirancrazy.smartmedical.pojo.Warehouse;
import com.yirancrazy.smartmedical.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 仓库服务实现类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseMapper warehouseMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertWarehouse(Warehouse warehouse) {
        return warehouseMapper.insert(warehouse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Warehouse getWarehouseById(Long id) {
        return warehouseMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer updateWarehouseById(Warehouse warehouse) {
        return warehouseMapper.updateById(warehouse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deleteWarehouseById(Long id) {
        return warehouseMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Warehouse> listAllWarehouses() {
        return warehouseMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<Warehouse> listWarehousesByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Warehouse> warehouses = warehouseMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(warehouses);
    }
}
