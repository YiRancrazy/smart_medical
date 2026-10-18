package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.OrderType;
import com.yirancrazy.smartmedical.service.OrderTypeService;
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
public class OrderTypeManager {

    private final OrderTypeService orderTypeService;

    /**
     * 新增订单类型（补雪花 ID 后入库）
     * @param orderType 订单类型实体
     * @return 影响行数
     */
    public int addOrderType(OrderType orderType) {
        Long id = IdUtil.getSnowflakeNextId();
        orderType.setId(id);
        return orderTypeService.insertOrderType(orderType);
    }

    /**
     * 按 ID 查询订单类型
     * @param id 订单类型 ID
     * @return 订单类型实体；不存在返回 null
     */
    public OrderType getOrderTypeById(Long id) {
        return orderTypeService.getOrderTypeById(id);
    }
}
