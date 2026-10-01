package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PatientManager 可访问患者集合单测
 * 验证 patientCardId 解析与授权校验由 Manager 编排、关系查询下沉 UserPatientRelationService。
 * @Author: YiRanCrazy@gmail.com
 * @Description: 患者可访问范围门面单测
 * @Datetime: 2026-09-05 10:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class PatientManagerTest {

    @Mock private PatientService patientService;
    @Mock private UserPatientRelationService userPatientRelationService;

    @InjectMocks
    private PatientManager patientManager;

    private static final Long CURRENT_USER_ID = 7L;

    @Test
    void getAccessiblePatientUserIds_nullCardId_delegatesToService() {
        List<Long> expected = List.of(7L, 8L);
        when(userPatientRelationService.listAccessiblePatientUserIds(CURRENT_USER_ID))
                .thenReturn(expected);

        List<Long> result = patientManager.getAccessiblePatientUserIds(CURRENT_USER_ID, null);

        assertEquals(expected, result);
        verify(userPatientRelationService).listAccessiblePatientUserIds(CURRENT_USER_ID);
        verify(patientService, never()).getPatientByPatientCardId(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void getAccessiblePatientUserIds_patientCardAuthorized_returnsPatientUserId() {
        Patient patient = new Patient();
        patient.setUserId(8L);

        when(patientService.getPatientByPatientCardId(100L)).thenReturn(patient);
        when(userPatientRelationService.hasAuthorization(CURRENT_USER_ID, 8L)).thenReturn(true);

        List<Long> result = patientManager.getAccessiblePatientUserIds(CURRENT_USER_ID, 100L);

        assertEquals(List.of(8L), result);
        verify(userPatientRelationService).hasAuthorization(CURRENT_USER_ID, 8L);
    }

    @Test
    void getAccessiblePatientUserIds_patientCardUnauthorized_returnsEmpty() {
        Patient patient = new Patient();
        patient.setUserId(8L);

        when(patientService.getPatientByPatientCardId(100L)).thenReturn(patient);
        when(userPatientRelationService.hasAuthorization(CURRENT_USER_ID, 8L)).thenReturn(false);

        List<Long> result = patientManager.getAccessiblePatientUserIds(CURRENT_USER_ID, 100L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAccessiblePatientUserIds_patientNotFound_returnsEmpty() {
        when(patientService.getPatientByPatientCardId(100L)).thenReturn(null);

        List<Long> result = patientManager.getAccessiblePatientUserIds(CURRENT_USER_ID, 100L);

        assertTrue(result.isEmpty());
        verify(userPatientRelationService, never()).hasAuthorization(CURRENT_USER_ID, 8L);
    }
}
