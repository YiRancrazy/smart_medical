//package com.yirancrazy.smartmedical.controller;
//
//import com.yirancrazy.smartmedical.manager.OrdersManager;
//import com.yirancrazy.smartmedical.pojo.Orders;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//
///**
// * @Author: YiRanCrazy@gmail.com
// * @Description:
// * @Datetime: 2026-02-02 13:57
// * @Version: 1.0
// */
//
//@RestController
//@RequestMapping("api/orders")
//@RequiredArgsConstructor
//@Tag(name = "订单管理", description = "订单相关接口")
//public class OrdersController {
//
//    private final OrdersManager ordersManager;
//
//    @PostMapping("/add")
//    @Operation(summary = "添加订单", description = "添加新订单")
//    public int addOrders(@RequestBody Orders orders) {
//        return ordersManager.addOrders(orders);
//    }
//
//    @GetMapping("/{id}")
//    @Operation(summary = "根据ID获取订单", description = "根据订单ID获取订单信息")
//    @Parameter(name = "id", description = "订单ID", required = true)
//    public Orders getOrdersById(@PathVariable String id) {
//        return ordersManager.getOrdersById(id);
//    }
//}
