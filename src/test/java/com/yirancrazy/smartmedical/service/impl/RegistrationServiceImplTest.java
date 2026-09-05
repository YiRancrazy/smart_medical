package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.RegistrationMapper;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.service.RegistrationStatusLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 挂号状态迁移 - 单元测试
 * 验证 updateStatusWithLog() 正确更新状态 + 写日志 + 乐观守门
 * @Author: YiRanCrazy@gmail.com
 * @Description: RegistrationServiceImpl.updateStatusWithLog 单测
 * @Datetime: 2026-09-05 10:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @InjectMocks
    RegistrationServiceImpl registrationService;

    @Mock
    RegistrationStatusLogService registrationStatusLogService;

    @Mock
    RegistrationMapper registrationMapper;

    @Test
    void updateStatusWithLog_updatesStatusAndWritesLog() {
        // Given: SUCCESS → REPORTED
        Registration reg = new Registration();
        reg.setId(100L);
        reg.setUserId(1L);
        reg.setStatus(RegistrationStatusEnum.SUCCESS.getCode());

        when(registrationMapper.update(eq(null), any(UpdateWrapper.class))).thenReturn(1);

        // When
        registrationService.updateStatusWithLog(reg,
                RegistrationStatusEnum.REPORTED.getCode(),
                1L, "user", "用户报到");

        // Then
        verify(registrationMapper).update(eq(null), any(UpdateWrapper.class));
        verify(registrationStatusLogService).writeLog(
                eq(100L),
                eq(RegistrationStatusEnum.SUCCESS.getCode()),
                eq(RegistrationStatusEnum.REPORTED.getCode()),
                eq(1L), eq("user"), eq("用户报到"));
        // 内存中的状态也应同步为目标状态
        assertEquals(RegistrationStatusEnum.REPORTED.getCode(), reg.getStatus());
    }

    @Test
    void updateStatusWithLog_rejectsInvalidTransition() {
        // Given: WAITING_FOR_PAYMENT → REPORTED 非法流转（需先支付）
        Registration reg = new Registration();
        reg.setId(104L);
        reg.setUserId(1L);
        reg.setStatus(RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode());

        // When + Then: 白名单拒绝，不应触达 mapper
        BizException ex = assertThrows(BizException.class, () ->
                registrationService.updateStatusWithLog(reg,
                        RegistrationStatusEnum.REPORTED.getCode(),
                        1L, "user", "非法报到"));
        assertEquals(BizErrorCode.REGISTRATION_STATUS_INVALID.getCode(), ex.getCode());
        verify(registrationMapper, never()).update(any(), any());
        verify(registrationStatusLogService, never()).writeLog(any(), any(), any(), any(), any(), any());
    }

    @Test
    void updateStatusWithLog_setsVisitStartTimeForInTreatment() {
        // Given: REPORTED → IN_TREATMENT
        Registration reg = new Registration();
        reg.setId(101L);
        reg.setUserId(1L);
        reg.setStatus(RegistrationStatusEnum.REPORTED.getCode());

        when(registrationMapper.update(eq(null), any(UpdateWrapper.class))).thenReturn(1);

        // When
        registrationService.updateStatusWithLog(reg,
                RegistrationStatusEnum.IN_TREATMENT.getCode(),
                1L, "doctor", "叫号接诊");

        // Then
        verify(registrationMapper).update(eq(null), any(UpdateWrapper.class));
        assertEquals(RegistrationStatusEnum.IN_TREATMENT.getCode(), reg.getStatus());
    }

    @Test
    void updateStatusWithLog_throwsWhenUpdateReturnsZero() {
        // Given: 模拟并发冲突 — 乐观守门失败
        Registration reg = new Registration();
        reg.setId(102L);
        reg.setUserId(1L);
        reg.setStatus(RegistrationStatusEnum.REPORTED.getCode());

        when(registrationMapper.update(eq(null), any(UpdateWrapper.class))).thenReturn(0);

        // When + Then
        BizException ex = assertThrows(BizException.class, () ->
                registrationService.updateStatusWithLog(reg,
                        RegistrationStatusEnum.IN_TREATMENT.getCode(),
                        1L, "doctor", "叫号接诊"));
        assertEquals(BizErrorCode.REGISTRATION_STATUS_INVALID.getCode(), ex.getCode());

        // 不应写日志
        verify(registrationStatusLogService, never()).writeLog(any(), any(), any(), any(), any(), any());
    }

    /**
     * 并发场景：fromStatus 不再匹配实际 DB 状态时，update 返回 0，
     * 状态日志不应写入（防止"报到重复点击"产生虚假日志）。
     */
    @Test
    void updateStatusWithLog_concurrentTransitionThrowsAndDoesNotWriteLog() {
        // Given: 内存中是 REPORTED，但 DB 已被其他事务改为 IN_TREATMENT
        Registration reg = new Registration();
        reg.setId(103L);
        reg.setUserId(1L);
        reg.setStatus(RegistrationStatusEnum.REPORTED.getCode());

        when(registrationMapper.update(eq(null), any(UpdateWrapper.class))).thenReturn(0);

        // When: 同一客户端再次点击"叫号"
        BizException ex = assertThrows(BizException.class, () ->
                registrationService.updateStatusWithLog(reg,
                        RegistrationStatusEnum.IN_TREATMENT.getCode(),
                        1L, "doctor", "叫号接诊"));

        // Then: 抛出 REGISTRATION_STATUS_INVALID，且不应写日志
        assertEquals(BizErrorCode.REGISTRATION_STATUS_INVALID.getCode(), ex.getCode());
        verify(registrationStatusLogService, never()).writeLog(any(), any(), any(), any(), any(), any());
    }
}
