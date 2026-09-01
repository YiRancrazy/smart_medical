package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.constant.OrderStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.OrdersMapper;
import com.yirancrazy.smartmedical.mapper.PaymentRecordMapper;
import com.yirancrazy.smartmedical.mapper.RegistrationScheduleMapper;
import com.yirancrazy.smartmedical.pojo.Order;
import com.yirancrazy.smartmedical.pojo.PaymentRecord;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RegistrationCheckInManager 单测
 * 覆盖：checkIn 当天校验、cancel 状态校验、退款联动（B20 null 守卫路径）。
 * @Author: YiRanCrazy@gmail.com
 * @Description: 报到/取消业务单测
 * @Datetime: 2026-07-24 15:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class RegistrationCheckInManagerTest {

    @Mock private RegistrationService registrationService;
    @Mock private RegistrationScheduleService registrationScheduleService;
    @Mock private RegistrationScheduleMapper registrationScheduleMapper;
    @Mock private PatientService patientService;
    @Mock private UserPatientRelationService userPatientRelationService;
    @Mock private OrdersMapper ordersMapper;
    @Mock private PaymentRecordMapper paymentRecordMapper;
    @Mock private RegistrationStatusLogManager statusLogManager;
    @Mock private RegistrationScheduleTemplateService registrationScheduleTemplateService;
    @Mock private OrderStatusLogManager orderStatusLogManager;

    @InjectMocks
    private RegistrationCheckInManager registrationCheckInManager;

    private static final Long REG_ID = 1001L;
    private static final Long USER_ID = 7L;
    private static final Long SCHEDULE_ID = 2001L;
    private static final Long TEMPLATE_ID = 3001L;
    private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Asia/Shanghai"));

    /**
     * checkIn happy path：status=SUCCESS + 当天排班 → 调 transition 到 REPORTED
     */
    @Test
    void checkIn_happyPath_transitionsToReported() {
        Registration reg = buildRegistration(REG_ID, USER_ID, RegistrationStatusEnum.SUCCESS.getCode(), SCHEDULE_ID);
        RegistrationSchedule schedule = buildSchedule(SCHEDULE_ID, TEMPLATE_ID);
        RegistrationScheduleTemplate template = buildTemplate(TEMPLATE_ID, TODAY);

        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);
        when(registrationScheduleService.getRegistrationScheduleById(SCHEDULE_ID)).thenReturn(schedule);
        when(registrationScheduleTemplateService.getRegistrationScheduleTemplateById(TEMPLATE_ID)).thenReturn(template);

        registrationCheckInManager.checkIn(REG_ID, USER_ID);

        verify(statusLogManager).transition(eq(reg), eq(RegistrationStatusEnum.REPORTED.getCode()),
                eq(USER_ID), eq("user"), eq("用户报到"));
    }

    /**
     * checkIn 未找到挂号记录 → REGISTRATION_NOT_FOUND
     */
    @Test
    void checkIn_notFound_throws() {
        when(registrationService.getRegistrationById(REG_ID)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> registrationCheckInManager.checkIn(REG_ID, USER_ID));
        assertEquals(BizErrorCode.REGISTRATION_NOT_FOUND.getCode(), ex.getCode());
    }

    /**
     * checkIn 非本人挂号 → REGISTRATION_NOT_OWNED
     */
    @Test
    void checkIn_notOwned_throws() {
        Registration reg = buildRegistration(REG_ID, 999L, RegistrationStatusEnum.SUCCESS.getCode(), SCHEDULE_ID);
        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);

        BizException ex = assertThrows(BizException.class,
                () -> registrationCheckInManager.checkIn(REG_ID, USER_ID));
        assertEquals(BizErrorCode.REGISTRATION_NOT_OWNED.getCode(), ex.getCode());
    }

    /**
     * checkIn 状态非 SUCCESS(1) → REGISTRATION_STATUS_INVALID
     */
    @Test
    void checkIn_wrongStatus_throws() {
        Registration reg = buildRegistration(REG_ID, USER_ID, RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode(), SCHEDULE_ID);
        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);

        BizException ex = assertThrows(BizException.class,
                () -> registrationCheckInManager.checkIn(REG_ID, USER_ID));
        assertEquals(BizErrorCode.REGISTRATION_STATUS_INVALID.getCode(), ex.getCode());
    }

    /**
     * checkIn 非当天排班 → REGISTRATION_STATUS_INVALID
     */
    @Test
    void checkIn_wrongDate_throws() {
        Registration reg = buildRegistration(REG_ID, USER_ID, RegistrationStatusEnum.SUCCESS.getCode(), SCHEDULE_ID);
        RegistrationSchedule schedule = buildSchedule(SCHEDULE_ID, TEMPLATE_ID);
        // 排班日期为明天
        RegistrationScheduleTemplate template = buildTemplate(TEMPLATE_ID, TODAY.plusDays(1));

        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);
        when(registrationScheduleService.getRegistrationScheduleById(SCHEDULE_ID)).thenReturn(schedule);
        when(registrationScheduleTemplateService.getRegistrationScheduleTemplateById(TEMPLATE_ID)).thenReturn(template);

        BizException ex = assertThrows(BizException.class,
                () -> registrationCheckInManager.checkIn(REG_ID, USER_ID));
        assertEquals(BizErrorCode.REGISTRATION_STATUS_INVALID.getCode(), ex.getCode());
        verify(statusLogManager, never()).transition(any(), anyInt(), anyLong(), any(), any());
    }

    /**
     * cancel 状态=null → REGISTRATION_STATUS_INVALID（B20 守卫）
     */
    @Test
    void cancel_nullStatus_throws() {
        Registration reg = buildRegistration(REG_ID, USER_ID, null, SCHEDULE_ID);
        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);

        BizException ex = assertThrows(BizException.class,
                () -> registrationCheckInManager.cancel(REG_ID, USER_ID, "test"));
        assertEquals(BizErrorCode.REGISTRATION_STATUS_INVALID.getCode(), ex.getCode());
        // 状态异常时不应触号源恢复
        verify(registrationScheduleMapper, never()).update(any(), any());
    }

    /**
     * cancel 状态=REPORTED(5) 不可取消 → REGISTRATION_STATUS_INVALID
     */
    @Test
    void cancel_reportedStatus_throws() {
        Registration reg = buildRegistration(REG_ID, USER_ID, RegistrationStatusEnum.REPORTED.getCode(), SCHEDULE_ID);
        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);

        BizException ex = assertThrows(BizException.class,
                () -> registrationCheckInManager.cancel(REG_ID, USER_ID, null));
        assertEquals(BizErrorCode.REGISTRATION_STATUS_INVALID.getCode(), ex.getCode());
    }

    /**
     * cancel 待支付订单(status=0) → 关闭订单 + 写订单日志 + transition 到 CANCELED
     */
    @Test
    void cancel_waitingPaymentOrder_closesOrder() {
        Registration reg = buildRegistration(REG_ID, USER_ID, RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode(), SCHEDULE_ID);
        reg.setOrderId(5001L);
        Order order = new Order();
        order.setId(5001L);
        order.setStatus(OrderStatus.WAITING_FOR_PAYMENT.getCode());

        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);
        when(ordersMapper.selectById(5001L)).thenReturn(order);

        registrationCheckInManager.cancel(REG_ID, USER_ID, "测试取消");

        // 验证号源恢复
        verify(registrationScheduleMapper).update(eq(null), any());
        // 验证订单置为 CANCELED
        assertEquals(OrderStatus.CANCELED.getCode(), order.getStatus());
        // 验证写订单日志
        verify(orderStatusLogManager).addOrderStatusLog(any());
        // 验证挂号状态迁移
        verify(statusLogManager).transition(eq(reg), eq(RegistrationStatusEnum.CANCELED.getCode()),
                eq(USER_ID), eq("user"), eq("测试取消"));
    }

    /**
     * cancel 已支付订单(status=1) → 退款 + 订单置 REFUNDED
     */
    @Test
    void cancel_paidOrder_refundsAndTransitions() {
        Registration reg = buildRegistration(REG_ID, USER_ID, RegistrationStatusEnum.SUCCESS.getCode(), SCHEDULE_ID);
        reg.setOrderId(5002L);
        Order order = new Order();
        order.setId(5002L);
        order.setStatus(OrderStatus.PAID.getCode());
        order.setTotalAmount(5000);

        PaymentRecord origPay = new PaymentRecord();
        origPay.setId(6001L);
        origPay.setOrderId(5002L);
        origPay.setStatus(2);
        origPay.setPaymentMethodId(1);

        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);
        when(ordersMapper.selectById(5002L)).thenReturn(order);
        when(paymentRecordMapper.selectOne(any())).thenReturn(origPay);
        when(paymentRecordMapper.selectList(any())).thenReturn(List.of());

        registrationCheckInManager.cancel(REG_ID, USER_ID, null);

        // 验证订单置为 REFUNDED
        assertEquals(OrderStatus.REFUNDED.getCode(), order.getStatus());
        // 验证写退款流水
        verify(paymentRecordMapper).insert(any(PaymentRecord.class));
        // 验证原支付记录置为已退款
        assertEquals(4, origPay.getStatus());
        // 验证挂号状态迁移（reason 为空时用默认文案）
        verify(statusLogManager).transition(eq(reg), eq(RegistrationStatusEnum.CANCELED.getCode()),
                eq(USER_ID), eq("user"), eq("用户取消"));
    }

    /**
     * cancel 代理权限：非本人但有授权关系 → 允许取消
     */
    @Test
    void cancel_authorizedProxy_allowed() {
        Registration reg = buildRegistration(REG_ID, 888L, RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode(), SCHEDULE_ID);
        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);
        when(userPatientRelationService.hasAuthorization(USER_ID, 888L)).thenReturn(true);

        registrationCheckInManager.cancel(REG_ID, USER_ID, "代取消");

        verify(statusLogManager).transition(eq(reg), eq(RegistrationStatusEnum.CANCELED.getCode()),
                eq(USER_ID), eq("user"), eq("代取消"));
    }

    /**
     * cancel 无订单ID(orderId=null)：仅恢复号源+迁移状态，不操作订单相关
     */
    @Test
    void cancel_noOrderId_skipsOrderHandling() {
        Registration reg = buildRegistration(REG_ID, USER_ID, RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode(), SCHEDULE_ID);
        // orderId 留 null
        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);

        registrationCheckInManager.cancel(REG_ID, USER_ID, "无订单取消");

        verify(registrationScheduleMapper).update(eq(null), any());
        verify(ordersMapper, never()).selectById(anyLong());
        verify(statusLogManager).transition(eq(reg), eq(RegistrationStatusEnum.CANCELED.getCode()),
                eq(USER_ID), eq("user"), eq("无订单取消"));
    }

    // ===== 辅助构造方法 =====

    private Registration buildRegistration(Long id, Long userId, Integer status, Long scheduleId) {
        Registration reg = new Registration();
        reg.setId(id);
        reg.setUserId(userId);
        reg.setStatus(status);
        reg.setRegistrationScheduleId(scheduleId);
        return reg;
    }

    private RegistrationSchedule buildSchedule(Long id, Long templateId) {
        RegistrationSchedule schedule = new RegistrationSchedule();
        schedule.setId(id);
        schedule.setRegistrationScheduleTemplateId(templateId);
        return schedule;
    }

    private RegistrationScheduleTemplate buildTemplate(Long id, LocalDate date) {
        RegistrationScheduleTemplate template = new RegistrationScheduleTemplate();
        template.setId(id);
        template.setRegistrationDate(date);
        return template;
    }
}
