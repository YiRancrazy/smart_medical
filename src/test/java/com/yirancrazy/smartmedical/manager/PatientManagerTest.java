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
 * @Author: YiRanCrazy@gmail.com
 * @Description: 验证可访问范围统一使用 patient.id
 * @Datetime: 2026-10-22 15:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class PatientManagerTest {

    @Mock
    private PatientService patientService;

    @Mock
    private UserPatientRelationService userPatientRelationService;

    @InjectMocks
    private PatientManager patientManager;

    private static final Long CURRENT_USER_ID = 7L;

    private static final Long PATIENT_ID = 8L;

    private static final Long PATIENT_CARD_ID = 100L;

    @Test
    void getAccessiblePatientIds_nullCardId_delegatesToService() {
        List<Long> expected = List.of(7L, 8L);
        when(userPatientRelationService.listAccessiblePatientIds(CURRENT_USER_ID))
                .thenReturn(expected);

        List<Long> result = patientManager.getAccessiblePatientIds(CURRENT_USER_ID, null);

        assertEquals(expected, result);
        verify(userPatientRelationService).listAccessiblePatientIds(CURRENT_USER_ID);
        verify(patientService, never()).getPatientByPatientCardId(PATIENT_CARD_ID);
    }

    @Test
    void getAccessiblePatientIds_patientCardRelated_returnsPatientId() {
        Patient patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setPatientCardId(PATIENT_CARD_ID);

        when(patientService.getPatientByPatientCardId(PATIENT_CARD_ID)).thenReturn(patient);
        when(userPatientRelationService.hasPatientRelation(CURRENT_USER_ID, PATIENT_ID)).thenReturn(true);

        List<Long> result = patientManager.getAccessiblePatientIds(CURRENT_USER_ID, PATIENT_CARD_ID);

        assertEquals(List.of(PATIENT_ID), result);
        verify(userPatientRelationService).hasPatientRelation(CURRENT_USER_ID, PATIENT_ID);
    }

    @Test
    void getAccessiblePatientIds_patientCardUnauthorized_returnsEmpty() {
        Patient patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setPatientCardId(PATIENT_CARD_ID);

        when(patientService.getPatientByPatientCardId(PATIENT_CARD_ID)).thenReturn(patient);
        when(userPatientRelationService.hasPatientRelation(CURRENT_USER_ID, PATIENT_ID)).thenReturn(false);

        List<Long> result = patientManager.getAccessiblePatientIds(CURRENT_USER_ID, PATIENT_CARD_ID);

        assertTrue(result.isEmpty());
    }

    @Test
    void getAccessiblePatientIds_patientNotFound_returnsEmpty() {
        when(patientService.getPatientByPatientCardId(PATIENT_CARD_ID)).thenReturn(null);

        List<Long> result = patientManager.getAccessiblePatientIds(CURRENT_USER_ID, PATIENT_CARD_ID);

        assertTrue(result.isEmpty());
        verify(userPatientRelationService, never()).hasPatientRelation(CURRENT_USER_ID, PATIENT_ID);
    }
}
