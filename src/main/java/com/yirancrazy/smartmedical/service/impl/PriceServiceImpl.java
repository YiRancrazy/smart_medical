package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.PriceMapper;
import com.yirancrazy.smartmedical.pojo.Price;
import com.yirancrazy.smartmedical.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号价格服务实现类
 * @Datetime: 2026-02-28
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class PriceServiceImpl implements PriceService {

    private final PriceMapper priceMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertPrice(Price price) {
        return priceMapper.insert(price);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Price getPriceById(Long id) {
        return priceMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer updatePriceById(Price price) {
        return priceMapper.updateById(price);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deletePriceById(Long id) {
        return priceMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Price> listAllPrices() {
        return priceMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<Price> listPricesByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Price> prices = priceMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(prices);
    }
}
