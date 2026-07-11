package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.ProductionType;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 生产类型服务接口
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

public interface ProductionTypeService {

    /**
     * 添加生产类型
     * @param productionType 生产类型对象
     * @return 添加结果
     */
    Integer insertProductionType(ProductionType productionType);

    /**
     * 根据ID查询生产类型
     * @param id 类型ID
     * @return 生产类型对象
     */
    ProductionType getProductionTypeById(Long id);

    /**
     * 根据ID更新生产类型
     * @param productionType 生产类型对象
     * @return 更新结果
     */
    Integer updateProductionTypeById(ProductionType productionType);

    /**
     * 根据ID删除生产类型
     * @param id 类型ID
     * @return 删除结果
     */
    Integer deleteProductionTypeById(Long id);

    /**
     * 查询所有生产类型列表
     * @return 生产类型列表
     */
    List<ProductionType> listAllProductionTypes();

    /**
     * 分页查询生产类型列表
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<ProductionType> listProductionTypesByPage(Integer pageNum, Integer pageSize);
}
