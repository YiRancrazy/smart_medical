package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 订单状态日志服务接口
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

public interface OrderStatusLogService {

    /**
     * 添加订单状态日志
     * @param orderStatusLog 订单状态日志信息
     * @return 添加结果
     */
    int insertOrderStatusLog(OrderStatusLog orderStatusLog);

    /**
     * 根据ID获取订单状态日志信息
     * @param id 订单状态日志ID
     * @return 订单状态日志信息
     */
    OrderStatusLog getOrderStatusLogById(Long id);

    /**
     * 更新订单状态日志信息
     * @param orderStatusLog 订单状态日志信息
     * @return 更新结果
     */
    int updateOrderStatusLogById(OrderStatusLog orderStatusLog);

    /**
     * 根据ID删除订单状态日志
     * @param id 订单状态日志ID
     * @return 删除结果
     */
    int deleteOrderStatusLogById(Long id);

    /**
     * 获取所有订单状态日志列表
     * @return 订单状态日志列表
     */
    List<OrderStatusLog> listAllOrderStatusLogs();

    /**
     * 分页查询订单状态日志
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<OrderStatusLog> getOrderStatusLogPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除订单状态日志
     * @param ids 订单状态日志ID列表
     * @return 删除结果
     */
    int deleteBatch(List<Long> ids);
}
