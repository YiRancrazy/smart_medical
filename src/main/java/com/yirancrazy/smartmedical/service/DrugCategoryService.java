package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.DrugCategory;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品分类服务接口
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

public interface DrugCategoryService {

    /**
     * 插入药品分类
     * @param drugCategory 药品分类
     * @return 插入结果
     */
    Integer insertDrugCategory(DrugCategory drugCategory);

    /**
     * 根据ID查询药品分类
     * @param id ID
     * @return 药品分类
     */
    DrugCategory getDrugCategoryById(Long id);

    /**
     * 根据ID更新药品分类
     * @param drugCategory 药品分类
     * @return 更新结果
     */
    Integer updateDrugCategoryById(DrugCategory drugCategory);

    /**
     * 根据ID删除药品分类
     * @param id ID
     * @return 删除结果
     */
    Integer deleteDrugCategoryById(Long id);

    /**
     * 查询所有药品分类
     * @return 药品分类列表
     */
    List<DrugCategory> listAllDrugCategories();

    /**
     * 分页查询药品分类
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 药品分类列表
     */
    PageInfo<DrugCategory> listDrugCategoriesByPage(Integer pageNum, Integer pageSize);
}
