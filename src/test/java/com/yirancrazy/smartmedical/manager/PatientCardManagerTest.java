package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.DesensitizedUtil;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.UserPatientRelation;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PatientCardSimpleResponse;
import com.yirancrazy.smartmedical.pojo.vo.OutPatientCardBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.registration.confirm.RegistrationConfirmPatientCardVo;
import com.yirancrazy.smartmedical.service.PatientCardService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PatientCardManager 患者身份查询单测
 * @Author: YiRanCrazy@gmail.com
 * @Description: 验证就诊卡查询域全部使用 patient.id 与患者资料
 * @Datetime: 2026-10-22 15:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class PatientCardManagerTest {

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

    private static final Long OWNER_USER_ID = 9L;

    private static final Long PATIENT_ID = 10L;

    private static final Long PATIENT_CARD_ID = 11L;

    private static final String PHONE = "13812345678";

    @Test
    void getPatientCardById_resolvesCardThroughPatientId() {
        Patient patient = patient();
        PatientCard patientCard = patientCard();

        when(patientService.getPatientById(PATIENT_ID)).thenReturn(patient);
        when(patientCardService.getPatientCardById(PATIENT_CARD_ID)).thenReturn(patientCard);

        Result<PatientCard> result = patientCardManager.getPatientCardById(PATIENT_ID);

        assertEquals(PATIENT_CARD_ID, result.getData().getId());
        verify(patientService).getPatientById(PATIENT_ID);
        verify(patientCardService).getPatientCardById(PATIENT_CARD_ID);
    }

    @Test
    void getPatientCardDetailByRelationId_readsIdentityFromPatient() {
        UserPatientRelation relation = relation();
        Patient patient = patient();
        PatientCard patientCard = patientCard();

        when(userPatientRelationService.getUserPatientRelationById(RELATION_ID)).thenReturn(relation);
        when(userPatientRelationService.hasPatientRelation(CURRENT_USER_ID, PATIENT_ID)).thenReturn(true);
        when(patientService.getPatientById(PATIENT_ID)).thenReturn(patient);
        when(patientCardService.getPatientCardById(PATIENT_CARD_ID)).thenReturn(patientCard);

        Result<PatientCardSimpleResponse> result =
                patientCardManager.getPatientCardDetailByRelationId(CURRENT_USER_ID, RELATION_ID);

        PatientCardSimpleResponse data = result.getData();
        assertEquals(String.valueOf(PATIENT_ID), data.getPatientId());
        assertEquals(String.valueOf(CURRENT_USER_ID), data.getUserId());
        assertEquals("测试患者", data.getPatientName());
        assertEquals("110101199001011234", data.getPatientIdCard());
        assertEquals(PHONE, data.getPatientPhone());
        assertEquals(1, data.getPatientSex());
        verify(userPatientRelationService).hasPatientRelation(CURRENT_USER_ID, PATIENT_ID);
        verify(patientService).getPatientById(PATIENT_ID);
    }

    @Test
    void getAllPatientBaseInfoByUserId_usesPatientFieldsAndKeepsUserIdAsAccountId() {
        UserPatientRelation relation = relation();
        Patient patient = patient();
        PatientCard patientCard = patientCard();

        when(userPatientRelationService.listAccessiblePatientIds(CURRENT_USER_ID))
                .thenReturn(List.of(PATIENT_ID));
        when(userPatientRelationService.getUserPatientRelationsByUserId(CURRENT_USER_ID))
                .thenReturn(List.of(relation));
        when(patientService.getPatientsByIds(List.of(PATIENT_ID))).thenReturn(List.of(patient));
        when(patientCardService.getPatientCardsByIds(List.of(PATIENT_CARD_ID)))
                .thenReturn(List.of(patientCard));

        Result<List<RegistrationConfirmPatientCardVo>> result =
                patientCardManager.getAllPatientBaseInfoByUserId(CURRENT_USER_ID);

        RegistrationConfirmPatientCardVo data = result.getData().get(0);
        assertEquals(String.valueOf(PATIENT_ID), data.getPatientId());
        assertEquals(String.valueOf(CURRENT_USER_ID), data.getUserId());
        assertEquals(String.valueOf(OWNER_USER_ID), data.getPatientUserId());
        assertEquals("测试患者", data.getPatientName());
        assertEquals("110101199001011234", data.getPatientIdCard());
        assertEquals(DesensitizedUtil.mobilePhone(PHONE), data.getPatientPhone());
        assertEquals(1, data.getPatientSex());
    }

    @Test
    void getDefaultPatientBaseInfoByUid_readsIdentityFromPatient() {
        UserPatientRelation relation = relation();
        Patient patient = patient();
        PatientCard patientCard = patientCard();

        when(userPatientRelationService.getUserPatientRelationsByUserId(CURRENT_USER_ID))
                .thenReturn(List.of(relation));
        when(patientService.getPatientById(PATIENT_ID)).thenReturn(patient);
        when(patientCardService.getPatientCardById(PATIENT_CARD_ID)).thenReturn(patientCard);

        Result<OutPatientCardBaseInfo> result =
                patientCardManager.getDefaultPatientBaseInfoByUid(CURRENT_USER_ID);

        OutPatientCardBaseInfo data = result.getData();
        assertEquals(String.valueOf(PATIENT_ID), data.getPatientId());
        assertEquals("测试患者", data.getPatientName());
        assertEquals("110101199001011234", data.getPatientIdCard());
        assertEquals(PHONE, data.getPatientPhone());
        assertEquals(1, data.getPatientSex());
        assertTrue(data.getOutPatientBalance() != null);
    }

    private UserPatientRelation relation() {
        UserPatientRelation relation = new UserPatientRelation();
        relation.setId(RELATION_ID);
        relation.setUserId(CURRENT_USER_ID);
        relation.setPatientId(PATIENT_ID);
        relation.setRelation("本人");
        relation.setDefaulted(true);
        relation.setRemark("本人就诊卡");
        return relation;
    }

    private Patient patient() {
        Patient patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setName("测试患者");
        patient.setIdCard("110101199001011234");
        patient.setPhone(PHONE);
        patient.setSex(1);
        patient.setOwnerUserId(OWNER_USER_ID);
        patient.setPatientCardId(PATIENT_CARD_ID);
        return patient;
    }

    private PatientCard patientCard() {
        PatientCard patientCard = new PatientCard();
        patientCard.setId(PATIENT_CARD_ID);
        patientCard.setSn(20261022001L);
        patientCard.setOutpatientBalance(10000);
        return patientCard;
    }
}
