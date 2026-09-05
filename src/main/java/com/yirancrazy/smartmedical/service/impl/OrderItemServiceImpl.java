package com.yirancrazy.smartmedical.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.OrderItemMapper;
import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 订单明细服务实现类
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemMapper orderItemMapper;

    @Override
    public int insertOrderItem(OrderItem orderItem) {
        return orderItemMapper.insert(orderItem);
    }

    @Override
    public OrderItem getOrderItemById(Long id) {
        return orderItemMapper.selectById(id);
    }

    @Override
    public int updateOrderItemById(OrderItem orderItem) {
        return orderItemMapper.updateById(orderItem);
    }

    @Override
    public int deleteOrderItemById(Long id) {
        return orderItemMapper.deleteById(id);
    }

    @Override
    public List<OrderItem> listAllOrderItems() {
        return orderItemMapper.selectList(null);
    }

    @Override
    public PageInfo<OrderItem> getOrderItemPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<OrderItem> orderItems = orderItemMapper.selectList(null);
        return new PageInfo<>(orderItems);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        return orderItemMapper.deleteBatchIds(ids);
    }

    @Override
    public List<OrderItem> listOrdersItemByIds(List<Long> ids) {
        return orderItemMapper.selectByIds(ids);
    }
}
