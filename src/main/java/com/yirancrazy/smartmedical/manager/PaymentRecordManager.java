package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PaymentRecordSimpleResponse;
import com.yirancrazy.smartmedical.service.*;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支付记录管理层
 * @Datetime: 2026-02-26 07:35
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class PaymentRecordManager {
    private final PaymentRecordService paymentRecordService;
    private final PatientCardService patientCardService;
    private final PatientService patientService;
    private final OrderService orderService;
    private final OrderTypeService orderTypeService;
    private final OrderItemService orderItemService;
    private final ProductionTypeService productionTypeService;
    private final PayMethodService paymentMethodService;

    /**
     * 获取用户所有的缴费记录
     * @param userId 用户id
     * @return 缴费记录列表
     */
    public Result<List<PaymentRecordSimpleResponse>> listAllPaymentRecordsSimple(Long userId) {

        // 根据用户id 获取所有订单
        List<Order> orders = orderService.getOrdersByUserId(userId);
        // 根据用户id 获取用户所有的缴费记录
        List<PaymentRecord> paymentRecords = paymentRecordService.listAllPaymentRecordsByOrderId(
                orders.stream()
                        .map(Order::getId)
                        .collect(Collectors.toList())
        );


        // 获取所有订单类型
        List<OrderType> orderTypes = orderTypeService.listAllOrderTypes();

        // 根据订单id获取所有订单项
        List<OrderItem> orderItems = orderItemService.listOrdersItemByIds(orders.stream().map(Order::getId).collect(Collectors.toList()));

        List<PaymentMethod> allPaymentMethods = paymentMethodService.listAllPayMethods();
        
        
        List<PaymentRecordSimpleResponse> result = new ArrayList<>();
        for (PaymentRecord paymentRecord : paymentRecords) {
            PaymentRecordSimpleResponse item = new PaymentRecordSimpleResponse();
            Order order = orders.stream()
                    .filter(item1 -> item1.getId().equals(paymentRecord.getOrderId()))
                    .findFirst()
                    .orElse(null);
            assert order != null;
            OrderType orderType = orderTypes.stream()
                    .filter(item1 -> item1.getId().equals(order.getOrderTypeId()))
                    .findFirst()
                    .orElse(null);
            // 获取当前订单项
            List<OrderItem> currentOrderItems = orderItems.stream()
                    .filter(item1 -> item1.getOrderId().equals(order.getId()))
                    .toList();



            item.setPaymentRecordId(String.valueOf(paymentRecord.getId()));    // 设置支付记录id
            item.setPaymentTime(paymentRecord.getPaymentTime());
            item.setOrderId(String.valueOf(paymentRecord.getOrderId()));
            item.setOrderTypeId(String.valueOf(order.getOrderTypeId()));
            assert orderType != null;
            item.setOrderType( orderType.getName());
            item.setOrderItems(
                    currentOrderItems.stream()
                            .map(OrderItem::getProductionName)
                            .collect(Collectors.toList())
            );
            item.setPaymentMethodId(String.valueOf(paymentRecord.getPaymentMethodId()));
            item.setPaymentMethodName(String.valueOf(allPaymentMethods
                    .stream()
                    .map(item1-> false)
                    .findFirst()
                    .orElse(null)));
            item.setPaymentStatus(String.valueOf(paymentRecord.getStatus()));
            item.setPaymentAmount(String.valueOf(paymentRecord.getTotalAmount()));
            result.add(item);
        }

        return Result.success(result);
    }
}
