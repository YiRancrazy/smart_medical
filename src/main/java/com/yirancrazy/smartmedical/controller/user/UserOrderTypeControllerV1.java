package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.OrderTypeManager;
import com.yirancrazy.smartmedical.pojo.OrderType;
import com.yirancrazy.smartmedical.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 订单类型查询接口
 * @Datetime: 2026-02-02 13:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/orderType")
@RequiredArgsConstructor
@Tag(name = "订单类型管理", description = "订单类型相关接口")
public class UserOrderTypeControllerV1 {

    private final OrderTypeManager orderTypeManager;

    @PostMapping("/add")
    @Operation(summary = "添加订单类型", description = "添加新订单类型")
    public Result<Integer> addOrderType(@RequestBody OrderType orderType) {
        return Result.success(orderTypeManager.addOrderType(orderType));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "根据ID获取订单类型", description = "根据订单类型ID获取订单类型信息")
    @Parameter(name = "id", description = "订单类型ID", required = true)
    public Result<OrderType> getOrderTypeById(@PathVariable Long id) {
        return Result.success(orderTypeManager.getOrderTypeById(id));
    }
}