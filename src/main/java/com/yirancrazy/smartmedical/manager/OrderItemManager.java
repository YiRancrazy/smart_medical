package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.service.OrderItemService;
import com.yirancrazy.smartmedical.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class OrderItemManager {

    private final OrderItemService orderItemService;
    private final OrderService orderService;

    /**
     * 新增订单明细（校验订单归属，防止任意已登录用户向他人订单插入明细）
     * @param orderItem 订单明细
     * @param currentUserId 当前用户ID
     * @return 插入结果
     */
    public int addOrderItem(OrderItem orderItem, Long currentUserId) {
        if (orderItem.getOrderId() == null) {
            throw new BizException(BizErrorCode.ORDER_STATUS_INVALID, "订单ID不能为空");
        }
        Order order = orderService.getOrderById(orderItem.getOrderId());
        if (order == null || !currentUserId.equals(order.getUserId())) {
            throw new BizException(BizErrorCode.ORDER_NOT_OWNED);
        }
        Long id = IdUtil.getSnowflakeNextId();
        orderItem.setId(id);
        return orderItemService.insertOrderItem(orderItem);
    }

    /**
     * 按 ID 查询订单明细
     * @param id 订单明细 ID
     * @return 订单明细实体；不存在返回 null
     */
    public OrderItem getOrderItemById(Long id) {
        return orderItemService.getOrderItemById(id);
    }
}
