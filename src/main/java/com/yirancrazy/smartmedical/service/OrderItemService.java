package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.OrderItem;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 订单明细服务接口
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

public interface OrderItemService {

    /**
     * 添加订单明细
     * @param orderItem 订单明细信息
     * @return 添加结果
     */
    int insertOrderItem(OrderItem orderItem);

    /**
     * 根据ID获取订单明细信息
     * @param id 订单明细ID
     * @return 订单明细信息
     */
    OrderItem getOrderItemById(Long id);

    /**
     * 更新订单明细信息
     * @param orderItem 订单明细信息
     * @return 更新结果
     */
    int updateOrderItemById(OrderItem orderItem);

    /**
     * 根据ID删除订单明细
     * @param id 订单明细ID
     * @return 删除结果
     */
    int deleteOrderItemById(Long id);

    /**
     * 获取所有订单明细列表
     * @return 订单明细列表
     */
    List<OrderItem> listAllOrderItems();

    /**
     * 分页查询订单明细
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<OrderItem> getOrderItemPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除订单明细
     * @param ids 订单明细ID列表
     * @return 删除结果
     */
    int deleteBatch(List<Long> ids);

    /**
     * 根据ID列表获取订单明细列表
     * @param ids 订单明细ID列表
     * @return 订单明细列表
     */
    List<OrderItem> listOrdersItemByIds(List<Long> ids);
}
