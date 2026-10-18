package com.yirancrazy.smartmedical.service.impl;

import cn.hutool.core.util.IdUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.OrderStatusLogMapper;
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.service.OrderStatusLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 订单状态日志服务实现类
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class OrderStatusLogServiceImpl implements OrderStatusLogService {

    private final OrderStatusLogMapper orderStatusLogMapper;

    @Override
    public int insertOrderStatusLog(OrderStatusLog orderStatusLog) {
        return orderStatusLogMapper.insert(orderStatusLog);
    }

    @Override
    public OrderStatusLog getOrderStatusLogById(Long id) {
        return orderStatusLogMapper.selectById(id);
    }

    @Override
    public int updateOrderStatusLogById(OrderStatusLog orderStatusLog) {
        return orderStatusLogMapper.updateById(orderStatusLog);
    }

    @Override
    public int deleteOrderStatusLogById(Long id) {
        return orderStatusLogMapper.deleteById(id);
    }

    @Override
    public List<OrderStatusLog> listAllOrderStatusLogs() {
        return orderStatusLogMapper.selectList(null);
    }

    @Override
    public PageInfo<OrderStatusLog> getOrderStatusLogPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<OrderStatusLog> orderStatusLogs = orderStatusLogMapper.selectList(null);
        return new PageInfo<>(orderStatusLogs);
    }

    @Override
    public int deleteBatch(List<Long> ids) {
        return orderStatusLogMapper.deleteByIds(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addOrderStatusLog(OrderStatusLog orderStatusLog) {
        orderStatusLog.setId(IdUtil.getSnowflakeNextId());
        return orderStatusLogMapper.insert(orderStatusLog);
    }
}
