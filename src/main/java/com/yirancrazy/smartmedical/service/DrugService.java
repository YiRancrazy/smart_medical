package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Drug;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品信息服务接口
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

public interface DrugService {

    /**
     * 添加药品信息
     * @param drug 药品对象
     * @return 添加结果
     */
    Integer insertDrug(Drug drug);

    /**
     * 根据ID查询药品信息
     * @param id 药品ID
     * @return 药品对象
     */
    Drug getDrugById(Long id);

    /**
     * 根据ID更新药品信息
     * @param drug 药品对象
     * @return 更新结果
     */
    Integer updateDrugById(Drug drug);

    /**
     * 根据ID删除药品信息
     * @param id 药品ID
     * @return 删除结果
     */
    Integer deleteDrugById(Long id);

    /**
     * 查询所有药品列表
     * @return 药品列表
     */
    List<Drug> listAllDrugs();

    /**
     * 分页查询药品列表
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<Drug> listDrugsByPage(Integer pageNum, Integer pageSize);

    /**
     * 根据药品通用名或商品名模糊搜索
     * @param keyword 关键词
     * @return 药品列表
     */
    List<Drug> listDrugsByKeyword(String keyword);
}
