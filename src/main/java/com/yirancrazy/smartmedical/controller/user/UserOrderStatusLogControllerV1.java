package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.OrderStatusLogManager;
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/orderStatusLog")
@RequiredArgsConstructor
@Tag(name = "订单状态日志管理", description = "订单状态变更日志相关接口")
public class UserOrderStatusLogControllerV1 {

    private final OrderStatusLogManager orderStatusLogManager;

    @PostMapping("/add")
    @Operation(summary = "添加订单状态日志", description = "添加新订单状态变更日志")
    public Result<Integer> addOrderStatusLog(@RequestBody OrderStatusLog orderStatusLog) {
        return Result.success(orderStatusLogManager.addOrderStatusLog(orderStatusLog));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "根据ID获取订单状态日志", description = "根据订单状态日志ID获取日志信息")
    @Parameter(name = "id", description = "订单状态日志ID", required = true)
    public Result<OrderStatusLog> getOrderStatusLogById(@PathVariable Long id) {
        return Result.success(orderStatusLogManager.getOrderStatusLogById(id));
    }
}