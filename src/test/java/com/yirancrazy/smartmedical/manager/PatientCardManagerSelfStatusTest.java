package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.UserPatientRelation;
import com.yirancrazy.smartmedical.pojo.vo.SelfPatientCardStatus;
import com.yirancrazy.smartmedical.service.PatientCardService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 本人就诊卡状态单测
 * @Author: YiRanCrazy@gmail.com
 * @Description: 验证本人患者资料统一从 patient 读取
 * @Datetime: 2026-10-22 15:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class PatientCardManagerSelfStatusTest {

    @Mock
    private PatientCardService patientCardService;

    @Mock
    private UserPatientRelationService userPatientRelationService;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientCardManager patientCardManager;

    private static final Long CURRENT_USER_ID = 7L;

    private static final Long RELATION_ID = 8L;

    private static final Long PATIENT_ID = 10L;

    private static final Long PATIENT_CARD_ID = 11L;

    private static final String PHONE = "13812345678";

    @Test
    void getSelfPatientCardStatus_returnsPatientIdentityWhenSelfRelationExists() {
        UserPatientRelation selfRelation = new UserPatientRelation();
        selfRelation.setId(RELATION_ID);
        selfRelation.setUserId(CURRENT_USER_ID);
        selfRelation.setPatientId(PATIENT_ID);
        selfRelation.setRelation("本人");
        selfRelation.setDefaulted(true);

        Patient patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setName("张三");
        patient.setIdCard("110101199001011234");
        patient.setPhone(PHONE);
        patient.setSex(1);
        patient.setOwnerUserId(CURRENT_USER_ID);
        patient.setPatientCardId(PATIENT_CARD_ID);

        PatientCard patientCard = new PatientCard();
        patientCard.setId(PATIENT_CARD_ID);
        patientCard.setSn(20261022001L);

        when(patientService.getPatientByOwnerUserId(CURRENT_USER_ID)).thenReturn(patient);
        when(userPatientRelationService.getByUserIdAndPatientId(CURRENT_USER_ID, PATIENT_ID))
                .thenReturn(selfRelation);
        when(patientCardService.getPatientCardById(PATIENT_CARD_ID)).thenReturn(patientCard);

        Result<SelfPatientCardStatus> result =
                patientCardManager.getSelfPatientCardStatus(CURRENT_USER_ID);

        SelfPatientCardStatus data = result.getData();
        assertTrue(data.getCompleted());
        assertEquals(String.valueOf(PATIENT_ID), data.getPatientId());
        assertEquals("张三", data.getPatientName());
        assertEquals("110101199001011234", data.getPatientIdCard());
        assertEquals(PHONE, data.getPatientPhone());
        assertEquals(1, data.getPatientSex());
        assertTrue(data.getDefaultPatient());
    }

    @Test
    void getSelfPatientCardStatus_returnsEmptyPatientFieldsWithoutOwnedPatient() {
        when(patientService.getPatientByOwnerUserId(CURRENT_USER_ID)).thenReturn(null);

        Result<SelfPatientCardStatus> result =
                patientCardManager.getSelfPatientCardStatus(CURRENT_USER_ID);

        SelfPatientCardStatus data = result.getData();
        assertFalse(data.getCompleted());
        assertNull(data.getPatientId());
        assertNull(data.getPatientName());
        assertNull(data.getRelationId());
        assertTrue(data.getDefaultPatient());
        verifyNoInteractions(userPatientRelationService, patientCardService);
    }
}
