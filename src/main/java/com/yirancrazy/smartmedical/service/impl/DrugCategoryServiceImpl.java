package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.DrugCategoryMapper;
import com.yirancrazy.smartmedical.pojo.DrugCategory;
import com.yirancrazy.smartmedical.service.DrugCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品分类服务实现类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class DrugCategoryServiceImpl implements DrugCategoryService {

    private final DrugCategoryMapper drugCategoryMapper;

    /**
     * 插入药品分类
     * @param drugCategory 药品分类
     * @return 插入的行数
     */
    @Override
    public Integer insertDrugCategory(DrugCategory drugCategory) {
        return drugCategoryMapper.insert(drugCategory);
    }

    /**
     * 根据id获取药品分类
     * @param id id
     * @return 药品分类
     */
    @Override
    public DrugCategory getDrugCategoryById(Long id) {
        return drugCategoryMapper.selectById(id);
    }

    /**
     * 更新药品类别
     * @param drugCategory 药品类别
     * @return 更新的行数
     */
    @Override
    public Integer updateDrugCategoryById(DrugCategory drugCategory) {
        return drugCategoryMapper.updateById(drugCategory);
    }

    /**
     * 删除药品类别
     * @param id id
     * @return 删除的行数
     */
    @Override
    public Integer deleteDrugCategoryById(Long id) {
        return drugCategoryMapper.deleteById(id);
    }

    /**
     * 获取所有药品类别
     * @return 所有药品类别
     */
    @Override
    public List<DrugCategory> listAllDrugCategories() {
        return drugCategoryMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 分页获取所有药品类别
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页后的药品类别
     */
    @Override
    public PageInfo<DrugCategory> listDrugCategoriesByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<DrugCategory> drugCategories = drugCategoryMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(drugCategories);
    }
}
