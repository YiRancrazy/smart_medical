package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.service.OrderStatusLogService;
import lombok.RequiredArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class OrderStatusLogManager {

    private final OrderStatusLogService orderStatusLogService;

    public int addOrderStatusLog(OrderStatusLog orderStatusLog) {
        Long id = IdUtil.getSnowflakeNextId();
        orderStatusLog.setId(id);
        return orderStatusLogService.insertOrderStatusLog(orderStatusLog);
    }

    public OrderStatusLog getOrderStatusLogById(Long id) {
        return orderStatusLogService.getOrderStatusLogById(id);
    }
}
