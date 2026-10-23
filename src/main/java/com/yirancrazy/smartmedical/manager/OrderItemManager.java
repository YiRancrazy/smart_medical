package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.service.OrderItemService;
import com.yirancrazy.smartmedical.service.OrderService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import lombok.RequiredArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class OrderItemManager {

    private final OrderItemService orderItemService;
    private final OrderService orderService;
    private final UserPatientRelationService userPatientRelationService;

    /**
     * 新增订单明细（校验订单归属或患者关系，防止任意已登录用户向他人订单插入明细）
     * @param orderItem 订单明细
     * @param currentUserId 当前用户ID
     * @return 插入结果
     */
    public int addOrderItem(OrderItem orderItem, Long currentUserId) {
        if (orderItem.getOrderId() == null) {
            throw new BizException(BizErrorCode.ORDER_STATUS_INVALID, "订单ID不能为空");
        }
        Order order = orderService.getOrderById(orderItem.getOrderId());
        if (!canAccessOrder(order, currentUserId)) {
            throw new BizException(BizErrorCode.ORDER_NOT_OWNED);
        }
        Long id = IdUtil.getSnowflakeNextId();
        orderItem.setId(id);
        return orderItemService.insertOrderItem(orderItem);
    }

    /**
     * 按 ID 查询订单明细并校验访问权限
     * @param id 订单明细 ID
     * @param currentUserId 当前用户ID
     * @return 订单明细实体；不存在返回 null
     * @throws BizException ORDER_NOT_OWNED 订单明细不属于当前用户且当前用户与其患者无有效关系
     */
    public OrderItem getOrderItemById(Long id, Long currentUserId) {
        OrderItem orderItem = orderItemService.getOrderItemById(id);
        if (orderItem == null) {
            return null;
        }
        if (orderItem.getOrderId() == null) {
            throw new BizException(BizErrorCode.ORDER_NOT_OWNED);
        }
        Order order = orderService.getOrderById(orderItem.getOrderId());
        if (!canAccessOrder(order, currentUserId)) {
            throw new BizException(BizErrorCode.ORDER_NOT_OWNED);
        }
        return orderItem;
    }

    /**
     * 判断当前账号是否可访问订单
     * @param order 订单
     * @param currentUserId 当前用户ID
     * @return 付款账号本人，或与订单患者存在有效关系时返回 true
     */
    private boolean canAccessOrder(Order order, Long currentUserId) {
        if (order == null || currentUserId == null) {
            return false;
        }
        if (currentUserId.equals(order.getUserId())) {
            return true;
        }
        return order.getPatientId() != null
                && userPatientRelationService.hasPatientRelation(currentUserId, order.getPatientId());
    }

}
