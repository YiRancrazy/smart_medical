package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.OrderType;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 订单类型服务接口
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

public interface OrderTypeService {

    /**
     * 添加订单类型
     * @param orderType 订单类型信息
     * @return 添加结果
     */
    int insertOrderType(OrderType orderType);

    /**
     * 根据ID获取订单类型信息
     * @param id 订单类型ID
     * @return 订单类型信息
     */
    OrderType getOrderTypeById(Long id);

    /**
     * 更新订单类型信息
     * @param orderType 订单类型信息
     * @return 更新结果
     */
    int updateOrderTypeById(OrderType orderType);

    /**
     * 根据ID删除订单类型
     * @param id 订单类型ID
     * @return 删除结果
     */
    int deleteOrderTypeById(Long id);

    /**
     * 获取所有订单类型列表
     * @return 订单类型列表
     */
    List<OrderType> listAllOrderTypes();

    /**
     * 分页查询订单类型
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<OrderType> getOrderTypePage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除订单类型
     * @param ids 订单类型ID列表
     * @return 删除结果
     */
    int deleteBatch(List<Long> ids);
}
