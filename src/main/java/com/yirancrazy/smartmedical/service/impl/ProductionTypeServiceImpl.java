package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.ProductionTypeMapper;
import com.yirancrazy.smartmedical.pojo.ProductionType;
import com.yirancrazy.smartmedical.service.ProductionTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 生产类型服务实现类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class ProductionTypeServiceImpl implements ProductionTypeService {

    private final ProductionTypeMapper productionTypeMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertProductionType(ProductionType productionType) {
        return productionTypeMapper.insert(productionType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProductionType getProductionTypeById(Long id) {
        return productionTypeMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer updateProductionTypeById(ProductionType productionType) {
        return productionTypeMapper.updateById(productionType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deleteProductionTypeById(Long id) {
        return productionTypeMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ProductionType> listAllProductionTypes() {
        return productionTypeMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<ProductionType> listProductionTypesByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ProductionType> productionTypes = productionTypeMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(productionTypes);
    }
}
