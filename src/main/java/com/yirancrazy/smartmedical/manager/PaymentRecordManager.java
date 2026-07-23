package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.OrdersMapper;
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
    private final OrdersMapper ordersMapper;
    private final OrderTypeService orderTypeService;
    private final OrderItemService orderItemService;
    private final ProductionTypeService productionTypeService;
    private final PayMethodService paymentMethodService;
    private final PrescriptionManager prescriptionManager;
    private final RegistrationService registrationService;
    private final RegistrationStatusLogManager registrationStatusLogManager;
    private final OrderStatusLogManager orderStatusLogManager;

    /**
     * 获取用户所有的缴费记录
     * ponytail: S25 已知限制 — 已批量查 orders/orderTypes/orderItems/paymentMethods（非 DB N+1），
     *           但无分页全量返回；数据量增大时需加分页参数 + 前端联动，留待后续
     * @param userId 用户id
     * @return 缴费记录列表
     */
    public Result<List<PaymentRecordSimpleResponse>> listAllPaymentRecordsSimple(Long userId) {

        // 根据用户id 获取所有订单
        List<Order> orders = orderService.getOrdersByUserId(userId);
        if (orders == null || orders.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toList());
        // 根据用户id 获取用户所有的缴费记录
        List<PaymentRecord> paymentRecords = paymentRecordService.listAllPaymentRecordsByOrderId(orderIds);


        // 获取所有订单类型
        List<OrderType> orderTypes = orderTypeService.listAllOrderTypes();

        // 根据订单id获取所有订单项
        List<OrderItem> orderItems = orderItemService.listOrdersItemByIds(orderIds);

        List<PaymentMethod> allPaymentMethods = paymentMethodService.listAllPayMethods();


        List<PaymentRecordSimpleResponse> result = new ArrayList<>();
        for (PaymentRecord paymentRecord : paymentRecords) {
            PaymentRecordSimpleResponse item = new PaymentRecordSimpleResponse();
            Order order = orders.stream()
                    .filter(item1 -> item1.getId().equals(paymentRecord.getOrderId()))
                    .findFirst()
                    .orElse(null);
            if (order == null) continue;
            OrderType orderType = orderTypes.stream()
                    .filter(item1 -> item1.getId().equals(order.getOrderTypeId()))
                    .findFirst()
                    .orElse(null);
            // 获取当前订单项
            List<OrderItem> currentOrderItems = orderItems.stream()
                    .filter(item1 -> item1.getOrderId().equals(order.getId()))
                    .toList();

            if (orderType == null) continue;

            item.setPaymentRecordId(String.valueOf(paymentRecord.getId()));    // 设置支付记录id
            item.setPaymentTime(paymentRecord.getPaymentTime());
            item.setOrderId(String.valueOf(paymentRecord.getOrderId()));
            item.setOrderTypeId(String.valueOf(order.getOrderTypeId()));
            item.setOrderType( orderType.getName());
            item.setOrderItems(
                    currentOrderItems.stream()
                            .map(OrderItem::getProductionName)
                            .collect(Collectors.toList())
            );
            item.setPaymentMethodId(String.valueOf(paymentRecord.getPaymentMethodId()));
            item.setPaymentMethodName(allPaymentMethods.stream()
                    .filter(pm -> pm.getId().equals(paymentRecord.getPaymentMethodId()))
                    .findFirst()
                    .map(PaymentMethod::getName)
                    .orElse("未知"));
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
        // 幂等:已支付则联动校验挂号状态并返回
        if (order.getStatus() != null && order.getStatus() == OrderStatus.PAID.getCode()) {
            log.info("[payment-success] orderId={} already paid, skip", orderId);
            syncRegistrationStatusPaid(orderId);
            return Result.success(null);
        }
        if (order.getStatus() == null || order.getStatus() != OrderStatus.WAITING_FOR_PAYMENT.getCode()) {
            throw new BizException(BizErrorCode.ORDER_STATUS_INVALID, "订单当前状态不可支付");
        }

        // 金额校验：实付金额必须等于订单应付金额
        Integer expected = order.getTotalAmount() == null ? 0 : order.getTotalAmount();
        Integer actual = realAmount != null ? realAmount : expected;
        if (!expected.equals(actual)) {
            throw new BizException(BizErrorCode.ORDER_STATUS_INVALID,
                    "支付金额不一致：应付=" + expected + "，实付=" + actual);
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

        // 3. 原子更新 Order.status: WAITING_FOR_PAYMENT → PAID，并发幂等
        int updated = ordersMapper.update(null,
                new UpdateWrapper<Order>()
                        .eq("id", orderId)
                        .eq("status", OrderStatus.WAITING_FOR_PAYMENT.getCode())
                        .set("status", OrderStatus.PAID.getCode()));
        if (updated == 0) {
            log.info("[payment-success] orderId={} concurrent paid, skip", orderId);
            syncRegistrationStatusPaid(orderId);
            return Result.success(null);
        }

        // S24: 写订单状态变更日志 WAITING_FOR_PAYMENT → PAID
        OrderStatusLog orderLog = new OrderStatusLog();
        orderLog.setOrderId(orderId);
        orderLog.setFromStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());
        orderLog.setToStatus(OrderStatus.PAID.getCode());
        orderLog.setOperatorId(0L);
        orderLog.setOperatorRole("system");
        orderLog.setRemark("支付成功");
        orderStatusLogManager.addOrderStatusLog(orderLog);

        // 4. 联动挂号:标记挂号为支付成功/待就诊
        Registration registration = registrationService.getRegistrationByOrderId(orderId);
        if (registration != null) {
            registrationStatusLogManager.transition(registration,
                    RegistrationStatusEnum.SUCCESS.getCode(),
                    registration.getUserId(), "user", "支付成功");
        }

        // 5. 联动处方:标记处方为已支付
        prescriptionManager.markAsPaid(orderId);

        log.info("[payment-success] orderId={}, paymentMethodId={}, realAmount={}",
                orderId, paymentMethodId, realAmount);
        return Result.success(null);
    }

    /**
     * 同步挂号状态为已支付（用于订单已支付但挂号未同步的兜底场景）
     * @param orderId 订单ID
     */
    private void syncRegistrationStatusPaid(Long orderId) {
        Registration registration = registrationService.getRegistrationByOrderId(orderId);
        // 仅当挂号仍处于待支付时才补同步，避免回退已报到/就诊中等状态
        if (registration != null
                && Integer.valueOf(RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode()).equals(registration.getStatus())) {
            registrationStatusLogManager.transition(registration,
                    RegistrationStatusEnum.SUCCESS.getCode(),
                    registration.getUserId(), "user", "支付成功(补同步)");
        }
    }
}