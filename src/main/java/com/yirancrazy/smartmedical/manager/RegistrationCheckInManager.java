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
        if (reg.getRegistrationScheduleId() != null) {
            RegistrationSchedule schedule = registrationScheduleService
                    .getRegistrationScheduleById(reg.getRegistrationScheduleId());
            RegistrationScheduleTemplate template = schedule == null ? null
                    : registrationScheduleTemplateService.getRegistrationScheduleTemplateById(schedule.getRegistrationScheduleTemplateId());
            if (template != null && template.getRegistrationDate() != null) {
                java.time.LocalDate scheduleDate = template.getRegistrationDate();
                java.time.LocalDate today = java.time.LocalDate.now();
                if (!scheduleDate.equals(today)) {
                    throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID,
                            "仅预约当天可报到");
                }
            }
        }
        statusLogManager.transition(reg,
                RegistrationStatusEnum.REPORTED.getCode(),
                userId, "user", "用户报到");
    }

    /**
     * 用户取消预约（status 0/1/5 → 3 + 退款）
     * @param regId 挂号记录ID
     * @param userId 当前用户ID
     * @param reason 取消原因
     * @throws BizException REGISTRATION_NOT_FOUND / REGISTRATION_NOT_OWNED / REGISTRATION_STATUS_INVALID
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long regId, Long userId, String reason) {
        Registration reg = registrationService.getRegistrationById(regId);
        if (reg == null) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_FOUND);
        }

        // 权限校验：挂号记录的 userId 或有代理权限
        boolean hasPermission = userId.equals(reg.getUserId());
        if (!hasPermission) {
            // 检查是否有代理权限（user_patient_relation 表）
            hasPermission = userPatientRelationService.hasAuthorization(userId, reg.getUserId());
        }
        if (!hasPermission) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_OWNED);
        }
        Integer curStatus = reg.getStatus();
        if (curStatus != null
                && curStatus != RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode()
                && curStatus != RegistrationStatusEnum.SUCCESS.getCode()) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID,
                    "当前状态不可取消");
        }

        // 恢复号源：取消成功后 remaining_quota + 1，若原为已满(2)则恢复为正常(1)
        if (reg.getRegistrationScheduleId() != null) {
            registrationScheduleMapper.update(null,
                    new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<RegistrationSchedule>()
                            .eq("id", reg.getRegistrationScheduleId())
                            .setSql("remaining_quota = remaining_quota + 1")
                            .setSql("status = IF(status = 2, 1, status)"));
        }

        // 取消时若有挂号订单则联动处理(关闭或退款)，按 reg.orderId 精确定位
        Order order = reg.getOrderId() == null ? null
                : ordersMapper.selectById(reg.getOrderId());
        if (order != null && order.getStatus() != null) {
            if (order.getStatus() == OrderStatus.WAITING_FOR_PAYMENT.getCode()) {
                // 待支付:直接关闭
                order.setStatus(OrderStatus.CANCELED.getCode());
                ordersMapper.updateById(order);
            } else if (order.getStatus() == OrderStatus.PAID.getCode()) {
                // 已支付:加载原支付记录 → 写退款流水 → 原记录置为已退款
                PaymentRecord orig = paymentRecordMapper.selectOne(
                        new LambdaQueryWrapper<PaymentRecord>()
                                .eq(PaymentRecord::getOrderId, order.getId())
                                .eq(PaymentRecord::getStatus, PAYMENT_STATUS_SUCCESS)
                                .last("LIMIT 1"));

                Integer refundAmount = order.getTotalAmount() != null ? order.getTotalAmount() : 0;
                PaymentRecord refund = new PaymentRecord();
                refund.setId(IdUtil.getSnowflakeNextId());
                refund.setOrderId(order.getId());
                refund.setSn(System.currentTimeMillis());
                refund.setTotalAmount(-refundAmount);
                refund.setRealAmount(-refundAmount);
                refund.setPaymentMethodId(orig != null && orig.getPaymentMethodId() != null
                        ? orig.getPaymentMethodId() : DEFAULT_PAYMENT_METHOD_ID);
                refund.setStatus(PAYMENT_STATUS_REFUNDED);
                refund.setPaymentTime(java.time.LocalDateTime.now());
                paymentRecordMapper.insert(refund);

                if (orig != null) {
                    orig.setStatus(PAYMENT_STATUS_REFUNDED);
                    paymentRecordMapper.updateById(orig);
                }

                // 订单置为已取消
                order.setStatus(OrderStatus.CANCELED.getCode());
                ordersMapper.updateById(order);
            }
        }

        statusLogManager.transition(reg,
                RegistrationStatusEnum.CANCELED.getCode(),
                userId, "user",
                reason != null && !reason.isEmpty() ? reason : "用户取消");
    }
}