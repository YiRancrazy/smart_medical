package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.OrderItemManager;
import com.yirancrazy.smartmedical.pojo.OrderItem;
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
@RequestMapping("api/user/v1/orderItem")
@RequiredArgsConstructor
@Tag(name = "订单明细管理", description = "订单明细相关接口")
public class UserOrderItemControllerV1 {

    private final OrderItemManager orderItemManager;

    @PostMapping("/add")
    @Operation(summary = "添加订单明细", description = "添加新订单明细")
    public int addOrderItem(@RequestBody OrderItem orderItem) {
        return orderItemManager.addOrderItem(orderItem);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取订单明细", description = "根据订单明细ID获取订单明细信息")
    @Parameter(name = "id", description = "订单明细ID", required = true)
    public OrderItem getOrderItemById(@PathVariable String id) {
        return orderItemManager.getOrderItemById(Long.parseLong(id));
    }
}
