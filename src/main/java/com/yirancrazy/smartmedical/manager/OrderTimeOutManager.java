package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.OrderTypeConstant;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.properties.ScheduledTaskProperties;
import com.yirancrazy.smartmedical.service.OrderService;
import com.yirancrazy.smartmedical.service.OrderStatusLogService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationService;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * 订单超时统一处理：定时扫描所有超时未支付订单，按订单类型联动作废挂号/处方并释放号源/锁定库存
 * @Author: YiRanCrazy@gmail.com
 * @Description: 订单超时管理器（挂号订单 + 处方药品订单统一在此过期作废）
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class OrderTimeOutManager {

    /** 系统操作人 ID/角色（写入状态日志） */
    private static final long SYSTEM_OPERATOR_ID = 0L;
    private static final String SYSTEM_OPERATOR_ROLE = "system";

    /** 订单支付超时时间：从订单创建时间起 30 分钟未支付自动作废 */
    private static final long PAYMENT_TIMEOUT_MINUTES = 30;

    private final OrderService orderService;
    private final OrderStatusLogService orderStatusLogService;
    private final PrescriptionService prescriptionService;
    private final RegistrationService registrationService;
    private final RegistrationScheduleService registrationScheduleService;
    private final ScheduledTaskProperties scheduledTaskProperties;

    @Scheduled(
        fixedDelayString = "#{@scheduledTaskProperties.timeoutScanIntervalMs}",
        initialDelayString = "#{@scheduledTaskProperties.initTimeoutScanIntervalMs}"
    )
    @Transactional(rollbackFor = Exception.class)
    public void closeExpiredNotPayOrders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.minusMinutes(PAYMENT_TIMEOUT_MINUTES);
        List<Order> expiredOrders = orderService.listExpiredNotPayOrders(deadline);
        if (expiredOrders.isEmpty()) {
            log.info("[order-timeout] 无超时未支付订单，跳过（截止 {}）", deadline);
            return;
        }
        log.info("[order-timeout] 检测到超时未支付订单 {} 条（截止 {}），开始自动作废", expiredOrders.size(), deadline);

        int closedCount = 0;
        int skippedCount = 0;
        for (Order expiredOrder : expiredOrders) {
            boolean closed = closeExpiredOrderWithDependencies(expiredOrder);
            if (closed) {
                closedCount++;
            } else {
                skippedCount++;
            }
        }
        log.info("[order-timeout] 本轮关闭 {} 条，跳过（状态已变更/无关联）{} 条，当前时间：{}",
                closedCount, skippedCount, now);
    }

    /**
     * 关闭单条超时订单并联动业务侧作废；任一环节状态被并发抢占则整单跳过并回滚
     * 关闭订单采用状态守卫（status 0→2），挂号/处方取消均为条件更新，先抢到者生效
     * @param order 超时待支付订单
     * @return true=成功关闭并完成联动；false=订单状态已变化（如已支付/已取消），跳过
     */
    private boolean closeExpiredOrderWithDependencies(Order order) {
        // 守卫关闭订单：仅待支付可置为已取消，避免覆盖并发支付/取消结果
        int orderRows = orderService.cancelOrderIfWaitingPayment(order.getId());
        if (orderRows == 0) {
            log.warn("[order-timeout] orderId={} 状态已变更（可能已支付/取消），跳过作废", order.getId());
            return false;
        }
        log.info("[order-timeout] 关闭待支付订单 orderId={}, sn={}, typeId={}, 创建时间={}",
                order.getId(), order.getSn(), order.getOrderTypeId(), order.getCreateTime());
        // 写订单状态流水：待支付(0) → 已取消(2)，操作人=system，便于追踪超时作废链路
        OrderStatusLog orderLog = new OrderStatusLog();
        orderLog.setOrderId(order.getId());
        orderLog.setFromStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());
        orderLog.setToStatus(OrderStatus.CANCELED.getCode());
        orderLog.setOperatorId(SYSTEM_OPERATOR_ID);
        orderLog.setOperatorRole(SYSTEM_OPERATOR_ROLE);
        orderLog.setRemark("订单支付超时自动取消");
        orderStatusLogService.addOrderStatusLog(orderLog);

        Long orderTypeId = order.getOrderTypeId();
        if (orderTypeId == null) {
            return true;
        }
        if (orderTypeId == OrderTypeConstant.REGISTRATION) {
            cancelExpiredRegistrationOrder(order);
        } else if (orderTypeId == OrderTypeConstant.DRUG) {
            cancelExpiredPrescriptionOrder(order);
        } else {
            log.warn("[order-timeout] orderId={} 未知订单类型 {}，仅关闭订单不联动", order.getId(), orderTypeId);
        }
        return true;
    }

    /**
     * 挂号订单超时联动：挂号 0→3（待支付→已取消）+ 释放号源 + 状态流水
     */
    private void cancelExpiredRegistrationOrder(Order order) {
        Registration reg = registrationService.getRegistrationByOrderId(order.getId());
        if (reg == null) {
            log.warn("[order-timeout] 挂号订单 orderId={} 未关联挂号记录，仅关闭订单", order.getId());
            return;
        }
        // 仅待支付挂号参与超时取消：已支付/已取消等状态由用户或支付回调处理，不在此重复流转
        if (reg.getStatus() == null
                || reg.getStatus() != RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode()) {
            log.warn("[order-timeout] 挂号订单 orderId={} 关联挂号 regId={} 状态={}，非待支付跳过",
                    order.getId(), reg.getId(), reg.getStatus());
            return;
        }
        // updateStatusWithLog 内部带状态白名单守卫 + 乐观更新，非法流转抛异常触发整体回滚
        registrationService.updateStatusWithLog(reg, RegistrationStatusEnum.CANCELED.getCode(),
                SYSTEM_OPERATOR_ID, SYSTEM_OPERATOR_ROLE, "订单支付超时自动取消");
        if (reg.getRegistrationScheduleId() != null) {
            registrationScheduleService.releaseQuota(reg.getRegistrationScheduleId());
        }
        log.info("[order-timeout] 挂号订单 orderId={} 关联挂号 regId={} 已取消并释放号源", order.getId(), reg.getId());
    }

    /**
     * 处方药品订单超时联动：处方 0→3（待支付→已取消）+ 释放锁定库存
     */
    private void cancelExpiredPrescriptionOrder(Order order) {
        Prescription rx = prescriptionService.getByOrderId(order.getId());
        if (rx == null) {
            log.warn("[order-timeout] 药品订单 orderId={} 未关联处方，仅关闭订单", order.getId());
            return;
        }
        boolean cancelled = prescriptionService.cancelExpiredPendingPrescription(rx.getId(),
                "订单支付超时自动作废");
        if (cancelled) {
            log.info("[order-timeout] 处方 orderId={} 关联处方 rxId={} 已作废并释放库存",
                    order.getId(), rx.getId());
        } else {
            log.warn("[order-timeout] 药品订单 orderId={} 关联处方 rxId={} 状态已变更，跳过", order.getId(), rx.getId());
        }
    }
}
