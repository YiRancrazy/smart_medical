package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.InventoryTransactionMapper;
import com.yirancrazy.smartmedical.pojo.InventoryTransaction;
import com.yirancrazy.smartmedical.service.InventoryTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 库存异动流水服务实现类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class InventoryTransactionServiceImpl implements InventoryTransactionService {

    private final InventoryTransactionMapper inventoryTransactionMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertInventoryTransaction(InventoryTransaction inventoryTransaction) {
        return inventoryTransactionMapper.insert(inventoryTransaction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InventoryTransaction getInventoryTransactionById(Long id) {
        return inventoryTransactionMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer updateInventoryTransactionById(InventoryTransaction inventoryTransaction) {
        return inventoryTransactionMapper.updateById(inventoryTransaction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deleteInventoryTransactionById(Long id) {
        return inventoryTransactionMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<InventoryTransaction> listAllInventoryTransactions() {
        return inventoryTransactionMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<InventoryTransaction> listInventoryTransactionsByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<InventoryTransaction> inventoryTransactions = inventoryTransactionMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(inventoryTransactions);
    }
}
