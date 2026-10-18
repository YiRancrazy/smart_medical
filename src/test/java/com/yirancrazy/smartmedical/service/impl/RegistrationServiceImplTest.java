package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.RegistrationMapper;
import com.yirancrazy.smartmedical.pojo.Registration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 挂号状态迁移 - 单元测试
 * 验证 updateStatusIfCurrent() 原子更新状态 + 乐观守门，不写日志
 * @Author: YiRanCrazy@gmail.com
 * @Description: RegistrationServiceImpl.updateStatusIfCurrent 单测
 * @Datetime: 2026-10-01 12:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @InjectMocks
    RegistrationServiceImpl registrationService;

    @Mock
    RegistrationMapper registrationMapper;

    @Test
    void updateStatusIfCurrent_returnsTrueAndSyncsStatus() {
        // Given: SUCCESS → REPORTED
        Registration reg = new Registration();
        reg.setId(100L);
        reg.setUserId(1L);
        reg.setStatus(RegistrationStatusEnum.SUCCESS.getCode());

        when(registrationMapper.update(eq(null), any(UpdateWrapper.class))).thenReturn(1);

        // When
        boolean updated = registrationService.updateStatusIfCurrent(
                reg, RegistrationStatusEnum.REPORTED.getCode());

        // Then
        assertTrue(updated);
        verify(registrationMapper).update(eq(null), any(UpdateWrapper.class));
        // 内存中的状态也应同步为目标状态
        assertEquals(RegistrationStatusEnum.REPORTED.getCode(), reg.getStatus());
    }

    @Test
    void updateStatusIfCurrent_rejectsInvalidTransition() {
        // Given: WAITING_FOR_PAYMENT → REPORTED 非法流转（需先支付）
        Registration reg = new Registration();
        reg.setId(104L);
        reg.setUserId(1L);
        reg.setStatus(RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode());

        // When + Then: 白名单拒绝，不应触达 mapper
        BizException ex = assertThrows(BizException.class, () ->
                registrationService.updateStatusIfCurrent(
                        reg, RegistrationStatusEnum.REPORTED.getCode()));
        assertEquals(BizErrorCode.REGISTRATION_STATUS_INVALID.getCode(), ex.getCode());
        verify(registrationMapper, never()).update(any(), any());
    }

    @Test
    void updateStatusIfCurrent_setsVisitStartTimeForInTreatment() {
        // Given: REPORTED → IN_TREATMENT
        Registration reg = new Registration();
        reg.setId(101L);
        reg.setUserId(1L);
        reg.setStatus(RegistrationStatusEnum.REPORTED.getCode());

        when(registrationMapper.update(eq(null), any(UpdateWrapper.class))).thenReturn(1);

        // When
        boolean updated = registrationService.updateStatusIfCurrent(
                reg, RegistrationStatusEnum.IN_TREATMENT.getCode());

        // Then
        assertTrue(updated);
        verify(registrationMapper).update(eq(null), any(UpdateWrapper.class));
        assertEquals(RegistrationStatusEnum.IN_TREATMENT.getCode(), reg.getStatus());
    }

    @Test
    void updateStatusIfCurrent_returnsFalseWhenUpdateReturnsZero() {
        // Given: 模拟并发冲突 — 乐观守门失败
        Registration reg = new Registration();
        reg.setId(102L);
        reg.setUserId(1L);
        reg.setStatus(RegistrationStatusEnum.REPORTED.getCode());

        when(registrationMapper.update(eq(null), any(UpdateWrapper.class))).thenReturn(0);

        // When
        boolean updated = registrationService.updateStatusIfCurrent(
                reg, RegistrationStatusEnum.IN_TREATMENT.getCode());

        // Then: 返回 false，且内存状态保持原值
        assertFalse(updated);
        assertEquals(RegistrationStatusEnum.REPORTED.getCode(), reg.getStatus());
    }

    /**
     * 并发场景：fromStatus 不再匹配实际 DB 状态时，update 返回 0，
     * 返回 false 由调用方决定是否写日志。
     */
    @Test
    void updateStatusIfCurrent_concurrentTransitionReturnsFalse() {
        // Given: 内存中是 REPORTED，但 DB 已被其他事务改为 IN_TREATMENT
        Registration reg = new Registration();
        reg.setId(103L);
        reg.setUserId(1L);
        reg.setStatus(RegistrationStatusEnum.REPORTED.getCode());

        when(registrationMapper.update(eq(null), any(UpdateWrapper.class))).thenReturn(0);

        // When
        boolean updated = registrationService.updateStatusIfCurrent(
                reg, RegistrationStatusEnum.IN_TREATMENT.getCode());

        // Then: 返回 false，且不推进内存状态
        assertFalse(updated);
        assertEquals(RegistrationStatusEnum.REPORTED.getCode(), reg.getStatus());
    }

    @ParameterizedTest
    @MethodSource("allowedTransitions")
    void updateStatusIfCurrent_allowsEveryDocumentedTransition(
            RegistrationStatusEnum fromStatus, RegistrationStatusEnum toStatus) {
        Registration reg = new Registration();
        reg.setId(200L);
        reg.setUserId(1L);
        reg.setStatus(fromStatus.getCode());

        when(registrationMapper.update(eq(null), any(UpdateWrapper.class))).thenReturn(1);

        boolean updated = registrationService.updateStatusIfCurrent(reg, toStatus.getCode());

        assertTrue(updated);
        assertEquals(toStatus.getCode(), reg.getStatus());
        verify(registrationMapper).update(eq(null), any(UpdateWrapper.class));
    }

    private static Stream<Arguments> allowedTransitions() {
        return Stream.of(
                Arguments.of(RegistrationStatusEnum.WAITING_FOR_PAYMENT, RegistrationStatusEnum.SUCCESS),
                Arguments.of(RegistrationStatusEnum.WAITING_FOR_PAYMENT, RegistrationStatusEnum.FAILED),
                Arguments.of(RegistrationStatusEnum.WAITING_FOR_PAYMENT, RegistrationStatusEnum.CANCELED),
                Arguments.of(RegistrationStatusEnum.SUCCESS, RegistrationStatusEnum.REPORTED),
                Arguments.of(RegistrationStatusEnum.SUCCESS, RegistrationStatusEnum.CANCELED),
                Arguments.of(RegistrationStatusEnum.FAILED, RegistrationStatusEnum.WAITING_FOR_PAYMENT),
                Arguments.of(RegistrationStatusEnum.FAILED, RegistrationStatusEnum.CANCELED),
                Arguments.of(RegistrationStatusEnum.REPORTED, RegistrationStatusEnum.IN_TREATMENT),
                Arguments.of(RegistrationStatusEnum.IN_TREATMENT, RegistrationStatusEnum.PENDING_PAYMENT),
                Arguments.of(RegistrationStatusEnum.IN_TREATMENT, RegistrationStatusEnum.COMPLETED),
                Arguments.of(RegistrationStatusEnum.PENDING_PAYMENT, RegistrationStatusEnum.IN_TREATMENT),
                Arguments.of(RegistrationStatusEnum.PENDING_PAYMENT, RegistrationStatusEnum.COMPLETED));
    }
}
