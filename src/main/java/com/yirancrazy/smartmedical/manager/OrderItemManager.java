package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.service.OrderItemService;
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

    public int addOrderItem(OrderItem orderItem) {
        Long id = IdUtil.getSnowflakeNextId();
        orderItem.setId(id);
        return orderItemService.insertOrderItem(orderItem);
    }

    public OrderItem getOrderItemById(Long id) {
        return orderItemService.getOrderItemById(id);
    }
}
