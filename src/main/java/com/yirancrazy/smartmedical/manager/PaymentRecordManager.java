package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PaymentRecordSimpleResponse;
import com.yirancrazy.smartmedical.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付记录管理层
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支付记录管理 + 支付成功回调(联动处方标记已支付)
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class PaymentRecordManager {
    /** 默认支付方式:4 现金 */
    private static final int DEFAULT_PAYMENT_METHOD_ID = 4;
    /** 支付记录状态:2 成功 */
    private static final int PAYMENT_STATUS_SUCCESS = 2;

    private final PaymentRecordService paymentRecordService;
    private final PatientCardService patientCardService;
    private final PatientService patientService;
    private final OrderService orderService;
    private final OrderTypeService orderTypeService;
    private final OrderItemService orderItemService;
    private final ProductionTypeService productionTypeService;
    private final PayMethodService paymentMethodService;
    private final PrescriptionManager prescriptionManager;

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

    /**
     * 支付成功回调:插入支付记录 + 订单置为已支付 + 联动处方
     * @param orderId 订单ID
     * @param paymentMethodId 支付方式ID(1微信 2支付宝 3医保 4现金)
     * @param transactionSn 第三方交易流水号
     * @param realAmount 实际支付金额(分)
     * @return 成功结果
     * @throws BizException ORDER_STATUS_INVALID 订单不存在或状态不允许支付
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> paySuccess(Long orderId, Integer paymentMethodId,
                                   Long transactionSn, Integer realAmount) {
        // 1. 加载 Order
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            throw new BizException(BizErrorCode.ORDER_STATUS_INVALID, "订单不存在");
        }
        // 幂等:已支付直接返回
        if (order.getStatus() != null && order.getStatus() == OrderStatus.PAID.getCode()) {
            log.info("[payment-success] orderId={} already paid, skip", orderId);
            return Result.success(null);
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.WAITING_FOR_PAYMENT.getCode()) {
            throw new BizException(BizErrorCode.ORDER_STATUS_INVALID, "订单当前状态不可支付");
        }

        // 2. 创建 PaymentRecord (status=2 成功)
        PaymentRecord record = new PaymentRecord();
        record.setId(IdUtil.getSnowflakeNextId());
        record.setSn(System.currentTimeMillis());
        record.setOrderId(orderId);
        record.setTotalAmount(order.getTotalAmount());
        record.setRealAmount(realAmount != null ? realAmount : order.getTotalAmount());
        record.setPaymentMethodId(paymentMethodId != null ? paymentMethodId : DEFAULT_PAYMENT_METHOD_ID);
        record.setStatus(PAYMENT_STATUS_SUCCESS);
        record.setTransactionSn(transactionSn);
        record.setPaymentTime(LocalDateTime.now());
        paymentRecordService.insertPaymentRecord(record);

        // 3. 改 Order.status: WAITING_FOR_PAYMENT → PAID
        order.setStatus(OrderStatus.PAID.getCode());
        orderService.updateOrderById(order);

        // 4. 联动处方:标记处方为已支付
        prescriptionManager.markAsPaid(orderId);

        log.info("[payment-success] orderId={}, paymentMethodId={}, realAmount={}",
                orderId, paymentMethodId, realAmount);
        return Result.success(null);
    }
}