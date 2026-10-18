package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.service.OrderStatusLogService;
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
public class OrderStatusLogManager {

    private final OrderStatusLogService orderStatusLogService;

    /**
     * 新增订单状态流水（补雪花 ID 后入库）
     * @param orderStatusLog 状态流水实体
     * @return 影响行数
     */
    public int addOrderStatusLog(OrderStatusLog orderStatusLog) {
        Long id = IdUtil.getSnowflakeNextId();
        orderStatusLog.setId(id);
        return orderStatusLogService.insertOrderStatusLog(orderStatusLog);
    }

    /**
     * 按 ID 查询订单状态流水
     * @param id 状态流水 ID
     * @return 状态流水实体；不存在返回 null
     */
    public OrderStatusLog getOrderStatusLogById(Long id) {
        return orderStatusLogService.getOrderStatusLogById(id);
    }
}
