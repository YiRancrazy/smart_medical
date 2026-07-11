//package com.yirancrazy.smartmedical.manager;
//
//import cn.hutool.core.util.IdUtil;
//import com.yirancrazy.smartmedical.annotation.Manager;
//import com.yirancrazy.smartmedical.pojo.Orders;
//import com.yirancrazy.smartmedical.service.OrdersService;
//import lombok.RequiredArgsConstructor;
//
///**
// * @Author: YiRanCrazy@gmail.com
// * @Description:
// * @Datetime: 2026-02-02 13:15
// * @Version: 1.0
// */
//
//@Manager
//@RequiredArgsConstructor
//public class OrdersManager {
//
//    private final OrdersService ordersService;
//
//    public int addOrders(Orders orders) {
//        orders.setOrderId(IdUtil.getSnowflakeNextIdStr());
//        return ordersService.addOrders(orders);
//    }
//
//    public Orders getOrdersById(String id) {
//        return ordersService.getOrdersById(id);
//    }
//}
