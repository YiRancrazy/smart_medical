package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Warehouse;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 仓库服务接口
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

public interface WarehouseService {

    /**
     * 添加仓库信息
     * @param warehouse 仓库对象
     * @return 添加结果
     */
    Integer insertWarehouse(Warehouse warehouse);

    /**
     * 根据ID查询仓库信息
     * @param id 仓库ID
     * @return 仓库对象
     */
    Warehouse getWarehouseById(Long id);

    /**
     * 根据ID更新仓库信息
     * @param warehouse 仓库对象
     * @return 更新结果
     */
    Integer updateWarehouseById(Warehouse warehouse);

    /**
     * 根据ID删除仓库信息
     * @param id 仓库ID
     * @return 删除结果
     */
    Integer deleteWarehouseById(Long id);

    /**
     * 查询所有仓库列表
     * @return 仓库列表
     */
    List<Warehouse> listAllWarehouses();

    /**
     * 分页查询仓库列表
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<Warehouse> listWarehousesByPage(Integer pageNum, Integer pageSize);
}
