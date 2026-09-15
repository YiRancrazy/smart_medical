package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.UserPatientRelation;
import com.yirancrazy.smartmedical.pojo.dto.user.response.PatientCardSimpleResponse;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.PatientCardService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * PatientCardManager 就诊人详情单测
 * @Author: YiRanCrazy@gmail.com
 * @Description: 验证编辑详情返回真实手机号，避免脱敏值被前端回传保存
 * @Datetime: 2026-09-14 10:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class PatientCardManagerTest {

    @Mock
    private PatientCardService patientCardService;

    @Mock
    private AccountService accountService;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private UserService userService;

    @Mock
    private UserPatientRelationService userPatientRelationService;

    @Mock
    private PatientService patientService;

    @InjectMocks
    private PatientCardManager patientCardManager;

    private static final Long CURRENT_USER_ID = 7L;

    private static final Long RELATION_ID = 8L;

    private static final Long PATIENT_USER_ID = 9L;

    private static final Long PATIENT_ID = 10L;

    private static final Long PATIENT_CARD_ID = 11L;

    private static final String PHONE = "13812345678";

    @Test
    void getPatientCardDetailByRelationId_returnsRawPhoneForEdit() {
        UserPatientRelation relation = new UserPatientRelation();
        relation.setId(RELATION_ID);
        relation.setUserId(CURRENT_USER_ID);
        relation.setPatientUserId(PATIENT_USER_ID);
        relation.setRelation("本人");
        relation.setDefaulted(true);

        Patient patient = new Patient();
        patient.setId(PATIENT_ID);
        patient.setPatientCardId(PATIENT_CARD_ID);

        User patientUser = new User();
        patientUser.setId(PATIENT_USER_ID);
        patientUser.setNickname("测试就诊人");
        patientUser.setIdCard("110101199001011234");

        Account patientAccount = new Account();
        patientAccount.setUserId(PATIENT_USER_ID);
        patientAccount.setPhone(PHONE);

        PatientCard patientCard = new PatientCard();
        patientCard.setId(PATIENT_CARD_ID);
        patientCard.setSn(20260914001L);

        when(userPatientRelationService.getUserPatientRelationById(RELATION_ID)).thenReturn(relation);
        when(patientService.getPatientByUserId(PATIENT_USER_ID)).thenReturn(patient);
        when(userService.getUserById(PATIENT_USER_ID)).thenReturn(patientUser);
        when(accountService.getAccountByUserId(PATIENT_USER_ID)).thenReturn(patientAccount);
        when(patientCardService.getPatientCardById(PATIENT_CARD_ID)).thenReturn(patientCard);

        Result<PatientCardSimpleResponse> result =
                patientCardManager.getPatientCardDetailByRelationId(CURRENT_USER_ID, RELATION_ID);

        assertEquals(PHONE, result.getData().getPatientPhone());
    }
}
