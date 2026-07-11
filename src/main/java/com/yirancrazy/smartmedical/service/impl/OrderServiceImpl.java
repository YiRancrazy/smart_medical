package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.OrdersMapper;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 订单服务实现类
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrdersMapper ordersMapper;

    /**
     * 添加订单
     * @param order 订单信息
     * @return 添加结果
     */
    @Override
    public int insertOrder(Order order) {
        return ordersMapper.insert(order);
    }

    /**
     * 根据ID获取订单信息
     * @param id 订单ID
     * @return 订单信息
     */
    @Override
    public Order getOrderById(Long id) {
        return ordersMapper.selectById(id);
    }

    /**
     * 根据订单号获取订单信息
     * @param orderSn 订单号
     * @return 订单信息
     */
    @Override
    public Order getOrderByOrderSn(String orderSn) {
        return ordersMapper.selectOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
    }

    /**
     * 更新订单信息
     * @param orders 订单信息
     * @return 更新结果
     */
    @Override
    public int updateOrderById(Order orders) {
        return ordersMapper.updateById(orders);
    }

    /**
     * 根据ID删除订单
     * @param id 订单ID
     * @return 删除结果
     */
    @Override
    public int deleteOrderById(Long id) {
        return ordersMapper.deleteById(id);
    }

    /**
     * 获取所有订单列表
     * @return 订单列表
     */
    @Override
    public List<Order> listAllOrders() {
        return ordersMapper.selectList(null);
    }

    /**
     * 分页查询订单
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Order> getOrderPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Order> orders = ordersMapper.selectList(null);
        return new PageInfo<>(orders);
    }

    /**
     * 批量删除订单
     * @param ids 订单ID列表
     * @return 删除结果
     */
    @Override
    public int deleteBatch(List<Long> ids) {
        return ordersMapper.deleteBatchIds(ids);
    }

    /**
     * 根据用户ID获取订单列表
     * @param userId 用户ID
     * @return 订单列表
     */
    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return ordersMapper.selectList(new QueryWrapper<Order>().eq("user_id", userId));
    }

    /**
     * 根据订单状态获取订单列表
     * @param status 订单状态
     * @return 订单列表
     */
    @Override
    public List<Order> getOrdersByStatus(String status) {
        return ordersMapper.selectList(new QueryWrapper<Order>().eq("status", status));
    }

    /**
     * 根据支付方式ID获取订单列表
     * @param payTypeId 支付方式ID
     * @return 订单列表
     */
    @Override
    public List<Order> getOrdersByPayTypeId(Long payTypeId) {
        return ordersMapper.selectList(new QueryWrapper<Order>().eq("pay_type_id", payTypeId));
    }

    /**
     * 获取订单总数
     * @return 订单总数
     */
    @Override
    public Long getOrderCount() {
        return ordersMapper.selectCount(null);
    }

    /**
     * 根据ID列表获取订单列表
     * @param ids ID列表
     * @return 订单列表
     */
    @Override
    public List<Order> listOrdersByIds(List<Long> ids) {
        return ordersMapper.selectList(new QueryWrapper<Order>().in("id", ids));
    }
}
