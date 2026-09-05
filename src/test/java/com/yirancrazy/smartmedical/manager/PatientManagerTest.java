package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PatientManager 薄门面单测
 * 验证可访问患者集合逻辑已委托给 UserPatientRelationService。
 * @Author: YiRanCrazy@gmail.com
 * @Description: 患者可访问范围门面单测
 * @Datetime: 2026-09-05 10:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class PatientManagerTest {

    @Mock private UserPatientRelationService userPatientRelationService;

    @InjectMocks
    private PatientManager patientManager;

    private static final Long CURRENT_USER_ID = 7L;

    @Test
    void getAccessiblePatientUserIds_delegatesToService() {
        List<Long> expected = List.of(7L, 8L);
        when(userPatientRelationService.getAccessiblePatientUserIds(CURRENT_USER_ID, null))
                .thenReturn(expected);

        List<Long> result = patientManager.getAccessiblePatientUserIds(CURRENT_USER_ID, null);

        assertEquals(expected, result);
        verify(userPatientRelationService).getAccessiblePatientUserIds(CURRENT_USER_ID, null);
    }
}
