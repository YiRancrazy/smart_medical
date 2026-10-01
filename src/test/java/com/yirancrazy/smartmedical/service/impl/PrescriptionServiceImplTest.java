package com.yirancrazy.smartmedical.service.impl;

import com.yirancrazy.smartmedical.mapper.PrescriptionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 处方 Service 状态守卫更新单测
 * @Author: YiRanCrazy@gmail.com
 * @Description: 覆盖条件更新原语的 null 短路与影响行数返回
 * @Datetime: 2026-10-01 02:40
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class PrescriptionServiceImplTest {

    @Mock
    private PrescriptionMapper prescriptionMapper;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(prescriptionService, "baseMapper", prescriptionMapper);
    }

    /**
     * 实现已不再注入库存/明细 Service，仅保留自身 Mapper
     */
    @Test
    void impl_onlyDependsOnOwnMapper() {
        assertNotNull(ReflectionTestUtils.getField(prescriptionService, "baseMapper"));
        List<String> serviceFields = Arrays.stream(PrescriptionServiceImpl.class.getDeclaredFields())
                .map(java.lang.reflect.Field::getName)
                .filter(name -> name.endsWith("Service"))
                .toList();
        assertEquals(0, serviceFields.size(), "PrescriptionServiceImpl 不应再注入其他 Service: " + serviceFields);
    }

    @Test
    void cancelPendingIfCurrent_null_returnsFalseWithoutUpdate() {
        assertFalse(prescriptionService.cancelPendingIfCurrent(null));
        verifyNoInteractions(prescriptionMapper);
    }

    @Test
    void cancelPendingIfCurrent_affectedOneRow_returnsTrue() {
        when(prescriptionMapper.update(any(), any())).thenReturn(1);

        assertTrue(prescriptionService.cancelPendingIfCurrent(4001L));

        verify(prescriptionMapper).update(any(), any());
    }

    @Test
    void cancelPendingIfCurrent_affectedZeroRow_returnsFalse() {
        when(prescriptionMapper.update(any(), any())).thenReturn(0);

        assertFalse(prescriptionService.cancelPendingIfCurrent(4001L));
    }

    @Test
    void applyRefundIfCurrent_affectedOneRow_returnsTrue() {
        when(prescriptionMapper.update(any(), any())).thenReturn(1);

        assertTrue(prescriptionService.applyRefundIfCurrent(4001L));

        verify(prescriptionMapper).update(any(), any());
    }
}
