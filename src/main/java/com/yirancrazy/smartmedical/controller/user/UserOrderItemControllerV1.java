package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.OrderItemManager;
import com.yirancrazy.smartmedical.pojo.OrderItem;
import com.yirancrazy.smartmedical.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 订单明细查询接口
 * @Datetime: 2026-02-02 13:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/orderItem")
@RequiredArgsConstructor
@Tag(name = "订单明细管理", description = "订单明细相关接口")
public class UserOrderItemControllerV1 {

    private final OrderItemManager orderItemManager;

    @PostMapping("/add")
    @Operation(summary = "添加订单明细", description = "添加新订单明细（校验订单归属）")
    public Result<Integer> addOrderItem(@RequestBody OrderItem orderItem,
                                        @RequestAttribute("currentUserId") Long currentUserId) {
        return Result.success(orderItemManager.addOrderItem(orderItem, currentUserId));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "根据ID获取订单明细", description = "根据订单明细ID获取订单明细信息")
    @Parameter(name = "id", description = "订单明细ID", required = true)
    public Result<OrderItem> getOrderItemById(@PathVariable Long id) {
        return Result.success(orderItemManager.getOrderItemById(id));
    }
}