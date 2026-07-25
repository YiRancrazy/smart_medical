package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Order;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 订单服务接口
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */
public interface OrderService {

    /**
     * 添加订单
     * @param order 订单信息
     * @return 添加结果
     */
    int insertOrder(Order order);

    /**
     * 根据ID获取订单信息
     * @param id 订单ID
     * @return 订单信息
     */
    Order getOrderById(Long id);

    /**
     * 根据订单号获取订单信息
     * @param orderSn 订单号
     * @return 订单信息
     */
    Order getOrderByOrderSn(String orderSn);

    /**
     * 更新订单信息
     * @param order 订单信息
     * @return 更新结果
     */
    int updateOrderById(Order order);

    /**
     * 根据ID删除订单
     * @param id 订单ID
     * @return 删除结果
     */
    int deleteOrderById(Long id);

    /**
     * 获取所有订单列表
     * @return 订单列表
     */
    List<Order> listAllOrders();

    /**
     * 分页查询订单
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<Order> getOrderPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除订单
     * @param ids 订单ID列表
     * @return 删除结果
     */
    int deleteBatch(List<Long> ids);

    /**
     * 根据用户ID获取订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    List<Order> getOrdersByUserId(Long userId);

    /**
     * 根据用户ID和订单类型ID获取订单列表
     * @param userId 用户ID
     * @param orderTypeId 订单类型ID
     * @return 订单列表
     */
    List<Order> getOrdersByUserIdAndOrderTypeId(Long userId, Long orderTypeId);

    /**
     * 根据用户ID和订单类型ID列表获取订单列表
     * @param userId 用户ID
     * @param orderTypeIds 订单类型ID列表
     * @return 订单列表
     */
    List<Order> getOrdersByUserIdAndOrderTypeIds(Long userId, List<Long> orderTypeIds);

    /**
     * 根据订单状态获取订单列表
     * @param status 订单状态
     * @return 订单列表
     */
    List<Order> getOrdersByStatus(String status);

    /**
     * 根据支付方式ID获取订单列表
     * @param payTypeId 支付方式ID
     * @return 订单列表
     */
    List<Order> getOrdersByPayTypeId(Long payTypeId);

    /**
     * 获取订单总数
     * @return 订单总数
     */
    Long getOrderCount();

    /**
     * 根据ID列表获取订单列表
     * @param ids ID列表
     * @return 订单列表
     */
    List<Order> listOrdersByIds(List<Long> ids);
}
