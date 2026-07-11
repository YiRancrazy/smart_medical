package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.OrderType;
import com.yirancrazy.smartmedical.service.OrderTypeService;
import lombok.RequiredArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class OrderTypeManager {

    private final OrderTypeService orderTypeService;

    public int addOrderType(OrderType orderType) {
        Long id = IdUtil.getSnowflakeNextId();
        orderType.setId(id);
        return orderTypeService.insertOrderType(orderType);
    }

    public OrderType getOrderTypeById(Long id) {
        return orderTypeService.getOrderTypeById(id);
    }
}
