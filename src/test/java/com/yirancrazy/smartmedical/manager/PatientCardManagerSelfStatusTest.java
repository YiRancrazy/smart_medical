package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.UserPatientRelation;
import com.yirancrazy.smartmedical.pojo.vo.SelfPatientCardStatus;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * 本人就诊卡状态单测
 * @Author: YiRanCrazy@gmail.com
 * @Description: 验证本人就诊卡完善判定与表单回填数据
 * @Datetime: 2026-09-14 00:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class PatientCardManagerSelfStatusTest {

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

    private static final Long PATIENT_CARD_ID = 11L;

    @Test
    void getSelfPatientCardStatus_returnsCompletedWhenSelfRelationExists() {
        User currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);
        currentUser.setUsername("张三");
        currentUser.setIdCard("110101199001011234");

        Account currentAccount = new Account();
        currentAccount.setUserId(CURRENT_USER_ID);
        currentAccount.setPhone("13812345678");

        UserPatientRelation selfRelation = new UserPatientRelation();
        selfRelation.setId(RELATION_ID);
        selfRelation.setUserId(CURRENT_USER_ID);
        selfRelation.setPatientUserId(CURRENT_USER_ID);
        selfRelation.setRelation("本人");
        selfRelation.setDefaulted(true);

        Patient patient = new Patient();
        patient.setUserId(CURRENT_USER_ID);
        patient.setPatientCardId(PATIENT_CARD_ID);

        PatientCard patientCard = new PatientCard();
        patientCard.setId(PATIENT_CARD_ID);
        patientCard.setSn(20260914001L);

        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(currentUser);
        when(accountService.getAccountByUserId(CURRENT_USER_ID)).thenReturn(currentAccount);
        when(userPatientRelationService.getUserPatientRelationsByUserId(CURRENT_USER_ID))
                .thenReturn(List.of(selfRelation));
        when(patientService.getPatientByUserId(CURRENT_USER_ID)).thenReturn(patient);
        when(patientCardService.getPatientCardById(PATIENT_CARD_ID)).thenReturn(patientCard);

        Result<SelfPatientCardStatus> result =
                patientCardManager.getSelfPatientCardStatus(CURRENT_USER_ID);

        assertTrue(result.getData().getCompleted());
        assertTrue(result.getData().getDefaultPatient());
    }

    @Test
    void getSelfPatientCardStatus_returnsIncompleteWithoutSelfRelation() {
        User currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);
        currentUser.setUsername("张三");
        currentUser.setIdCard("110101199001011234");

        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(currentUser);
        when(userPatientRelationService.getUserPatientRelationsByUserId(CURRENT_USER_ID))
                .thenReturn(List.of());

        Result<SelfPatientCardStatus> result =
                patientCardManager.getSelfPatientCardStatus(CURRENT_USER_ID);

        assertFalse(result.getData().getCompleted());
        assertTrue(result.getData().getDefaultPatient());
    }
}
