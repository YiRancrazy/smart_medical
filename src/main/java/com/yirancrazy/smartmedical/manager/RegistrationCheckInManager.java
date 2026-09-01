package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.OrdersMapper;
import com.yirancrazy.smartmedical.mapper.PaymentRecordMapper;
import com.yirancrazy.smartmedical.mapper.RegistrationScheduleMapper;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.OrderStatusLog;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.PaymentRecord;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户报到/取消业务编排
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户对挂号的报到与取消(含退款联动)
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class RegistrationCheckInManager {

    /** 支付记录状态:成功 */
    private static final int PAYMENT_STATUS_SUCCESS = 2;
    /** 支付记录状态:已退款 */
    private static final int PAYMENT_STATUS_REFUNDED = 4;
    /** 默认支付方式:4 现金 */
    private static final int DEFAULT_PAYMENT_METHOD_ID = 4;

    private final RegistrationService registrationService;
    private final RegistrationScheduleService registrationScheduleService;
    private final RegistrationScheduleMapper registrationScheduleMapper;
    private final PatientService patientService;
    private final UserPatientRelationService userPatientRelationService;
    private final OrdersMapper ordersMapper;
    private final PaymentRecordMapper paymentRecordMapper;
    private final RegistrationStatusLogManager statusLogManager;
    private final RegistrationScheduleTemplateService registrationScheduleTemplateService;
    private final OrderStatusLogManager orderStatusLogManager;

    /**
     * 用户报到（status 0/1 → 5）
     * @param regId 挂号记录ID
     * @param userId 当前用户ID
     * @throws BizException REGISTRATION_NOT_FOUND / REGISTRATION_NOT_OWNED / REGISTRATION_STATUS_INVALID
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkIn(Long regId, Long userId) {
        Registration reg = registrationService.getRegistrationById(regId);
        if (reg == null) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_FOUND);
        }
        if (!userId.equals(reg.getUserId())) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_OWNED);
        }
        Integer curStatus = reg.getStatus();
        if (curStatus == null || curStatus != RegistrationStatusEnum.SUCCESS.getCode()) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID,
                    "仅待就诊状态可报到");
        }
        // 校验预约当天才可报到
        if (reg.getRegistrationScheduleId() == null) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID, "排班信息缺失");
        }
        RegistrationSchedule schedule = registrationScheduleService
                .getRegistrationScheduleById(reg.getRegistrationScheduleId());
        if (schedule == null || schedule.getRegistrationScheduleTemplateId() == null) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID, "排班信息缺失");
        }
        RegistrationScheduleTemplate template = registrationScheduleTemplateService
                .getRegistrationScheduleTemplateById(schedule.getRegistrationScheduleTemplateId());
        if (template == null || template.getRegistrationDate() == null) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID, "排班模板信息缺失");
        }
        java.time.LocalDate scheduleDate = template.getRegistrationDate();
        // 强制 Asia/Shanghai 时区，避免容器默认 UTC 导致凌晨报到被拒
        java.time.LocalDate today = java.time.LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"));
        if (!scheduleDate.equals(today)) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID,
                    "仅预约当天可报到");
        }
        statusLogManager.transition(reg,
                RegistrationStatusEnum.REPORTED.getCode(),
                userId, "user", "用户报到");
    }

    /**
     * 用户取消预约（status 0/1 → 3 + 退款）
     * @param regId 挂号记录ID
     * @param userId 当前用户ID
     * @param reason 取消原因
     * @throws BizException REGISTRATION_NOT_FOUND / REGISTRATION_NOT_OWNED / REGISTRATION_STATUS_INVALID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long regId, Long userId, String reason) {
        // 1. 校验挂号存在 + 权限
        Registration reg = validateCancelPermission(regId, userId);

        // 2. 校验状态可取消
        validateCancelStatus(reg);

        // 3. 恢复号源
        restoreQuota(reg);

        // 4. 联动处理订单（关闭待支付 / 退款已支付）
        handleOrderForCancel(reg, userId);

        // 5. 状态迁移 → 已取消
        statusLogManager.transition(reg,
                RegistrationStatusEnum.CANCELED.getCode(),
                userId, "user",
                reason != null && !reason.isEmpty() ? reason : "用户取消");
    }

    // ========== cancel() 子方法 ==========

    /**
     * 校验挂号存在性 + 归属权限
     */
    private Registration validateCancelPermission(Long regId, Long userId) {
        Registration reg = registrationService.getRegistrationById(regId);
        if (reg == null) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_FOUND);
        }
        boolean hasPermission = userId.equals(reg.getUserId());
        if (!hasPermission) {
            hasPermission = userPatientRelationService.hasAuthorization(userId, reg.getUserId());
        }
        if (!hasPermission) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_OWNED);
        }
        return reg;
    }

    /**
     * 校验挂号状态可取消
     */
    private void validateCancelStatus(Registration reg) {
        Integer curStatus = reg.getStatus();
        if (curStatus == null) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID,
                    "挂号记录状态异常，不可取消");
        }
        if (curStatus != RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode()
                && curStatus != RegistrationStatusEnum.SUCCESS.getCode()) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID,
                    "当前状态不可取消");
        }
    }

    /**
     * 恢复号源：remaining_quota + 1，已满则恢复为正常
     */
    private void restoreQuota(Registration reg) {
        if (reg.getRegistrationScheduleId() != null) {
            registrationScheduleMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<RegistrationSchedule>()
                            .eq("id", reg.getRegistrationScheduleId())
                            .setSql("remaining_quota = remaining_quota + 1")
                            .setSql("status = IF(status = 2, 1, status)"));
        }
    }

    /**
     * 联动处理订单：待支付关闭，已支付退款
     */
    private void handleOrderForCancel(Registration reg, Long userId) {
        Order order = reg.getOrderId() == null ? null
                : ordersMapper.selectById(reg.getOrderId());
        if (order == null || order.getStatus() == null) {
            return;
        }
        if (order.getStatus() == OrderStatus.WAITING_FOR_PAYMENT.getCode()) {
            closeOrder(order, userId);
        } else if (order.getStatus() == OrderStatus.PAID.getCode()) {
            refundOrder(order, userId);
        }
    }

    /**
     * 关闭待支付订单
     */
    private void closeOrder(Order order, Long userId) {
        Integer fromStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELED.getCode());
        ordersMapper.updateById(order);
        OrderStatusLog orderLog = new OrderStatusLog();
        orderLog.setOrderId(order.getId());
        orderLog.setFromStatus(fromStatus);
        orderLog.setToStatus(OrderStatus.CANCELED.getCode());
        orderLog.setOperatorId(userId);
        orderLog.setOperatorRole("user");
        orderLog.setRemark("取消预约关闭订单");
        orderStatusLogManager.addOrderStatusLog(orderLog);
    }

    /**
     * 已支付订单退款：创建退款记录 + 原支付记录置为已退款 + 订单置为已退款
     */
    private void refundOrder(Order order, Long userId) {
        PaymentRecord orig = paymentRecordMapper.selectOne(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, order.getId())
                        .eq(PaymentRecord::getStatus, PAYMENT_STATUS_SUCCESS)
                        .last("LIMIT 1"));

        Integer refundAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0;
        List<PaymentRecord> refundedRecords = paymentRecordMapper.selectList(
                new LambdaQueryWrapper<PaymentRecord>()
                        .eq(PaymentRecord::getOrderId, order.getId())
                        .eq(PaymentRecord::getStatus, PAYMENT_STATUS_REFUNDED)
                        .lt(PaymentRecord::getRealAmount, 0));
        int alreadyRefunded = -refundedRecords.stream()
                .mapToInt(r -> r.getRealAmount() == null ? 0 : r.getRealAmount())
                .sum();
        int remainingRefund = refundAmount - alreadyRefunded;
        if (remainingRefund > 0) {
            PaymentRecord refund = new PaymentRecord();
            refund.setId(IdUtil.getSnowflakeNextId());
            refund.setOrderId(order.getId());
            refund.setSn(System.currentTimeMillis());
            refund.setTotalAmount(-remainingRefund);
            refund.setRealAmount(-remainingRefund);
            refund.setPaymentMethodId(orig != null && orig.getPaymentMethodId() != null
                    ? orig.getPaymentMethodId() : DEFAULT_PAYMENT_METHOD_ID);
            refund.setStatus(PAYMENT_STATUS_REFUNDED);
            refund.setPaymentTime(java.time.LocalDateTime.now());
            paymentRecordMapper.insert(refund);

            if (orig != null) {
                orig.setStatus(PAYMENT_STATUS_REFUNDED);
                paymentRecordMapper.updateById(orig);
            }
        } else {
            log.warn("[cancel] orderId={} 已全额退款(剩余可退={})，跳过退款写入",
                    order.getId(), remainingRefund);
        }

        Integer fromStatus = order.getStatus();
        order.setStatus(OrderStatus.REFUNDED.getCode());
        ordersMapper.updateById(order);
        OrderStatusLog orderLog = new OrderStatusLog();
        orderLog.setOrderId(order.getId());
        orderLog.setFromStatus(fromStatus);
        orderLog.setToStatus(OrderStatus.REFUNDED.getCode());
        orderLog.setOperatorId(userId);
        orderLog.setOperatorRole("user");
        orderLog.setRemark("取消预约退款");
        orderStatusLogManager.addOrderStatusLog(orderLog);
    }
}