package com.yirancrazy.smartmedical.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.OrderTypeMapper;
import com.yirancrazy.smartmedical.pojo.OrderType;
import com.yirancrazy.smartmedical.service.OrderTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 订单类型服务实现类
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class OrderTypeServiceImpl implements OrderTypeService {

    private final OrderTypeMapper orderTypeMapper;

    @Override
    public int insertOrderType(OrderType orderType) {
        return orderTypeMapper.insert(orderType);
    }

    @Override
    public OrderType getOrderTypeById(Long id) {
        return orderTypeMapper.selectById(id);
    }

    @Override
    public int updateOrderTypeById(OrderType orderType) {
        return orderTypeMapper.updateById(orderType);
    }

    @Override
    public int deleteOrderTypeById(Long id) {
        return orderTypeMapper.deleteById(id);
    }

    @Override
    public List<OrderType> listAllOrderTypes() {
        return orderTypeMapper.selectList(null);
    }

    @Override
    public PageInfo<OrderType> getOrderTypePage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<OrderType> orderTypes = orderTypeMapper.selectList(null);
        return new PageInfo<>(orderTypes);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        return orderTypeMapper.deleteByIds(ids);
    }
}
