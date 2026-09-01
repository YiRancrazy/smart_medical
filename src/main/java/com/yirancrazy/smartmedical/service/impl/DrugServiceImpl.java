package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.DrugMapper;
import com.yirancrazy.smartmedical.pojo.Drug;
import com.yirancrazy.smartmedical.service.DrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品信息服务实现类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class DrugServiceImpl implements DrugService {

    private final DrugMapper drugMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertDrug(Drug drug) {
        return drugMapper.insert(drug);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Drug getDrugById(Long id) {
        return drugMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer updateDrugById(Drug drug) {
        return drugMapper.updateById(drug);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deleteDrugById(Long id) {
        return drugMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Drug> listAllDrugs() {
        return drugMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<Drug> listDrugsByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Drug> drugs = drugMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(drugs);
    }

    @Override
    public List<Drug> listDrugsByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return drugMapper.selectList(new LambdaQueryWrapper<Drug>().last("LIMIT 20"));
        }
        return drugMapper.selectList(new LambdaQueryWrapper<Drug>()
                .like(Drug::getCommonName, keyword)
                .or()
                .like(Drug::getTradeName, keyword)
                .last("LIMIT 20"));
    }

    @Override
    public List<Drug> listDrugsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return drugMapper.selectBatchIds(ids);
    }
}
