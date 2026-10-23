package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.UserPatientRelation;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.PatientCardService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import com.yirancrazy.smartmedical.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 用户患者关系 Manager 单测
 * @Author: YiRanCrazy@gmail.com
 * @Description: 验证患者档案、本人绑定、关系删除与账号解耦
 * @Datetime: 2026-10-22 23:10
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserPatientRelationManagerTest {

    @Mock
    private UserPatientRelationService userPatientRelationService;

    @Mock
    private AccountService accountService;

    @Mock
    private UserService userService;

    @Mock
    private PatientCardService patientCardService;

    @Mock
    private PatientService patientService;

    @Mock
    private RegistrationService registrationService;

    @InjectMocks
    private UserPatientRelationManager userPatientRelationManager;

    private static final Long CURRENT_USER_ID = 100L;

    private static final Long PATIENT_ID = 200L;

    private static final Long RELATION_ID = 300L;

    private static final String ID_CARD = "11010519491231002X";

    private static final String LOWER_ID_CARD = "11010519491231002x";

    private static final String PHONE = "17330668339";

    @Test
    void insertFamilyCreatesPatientCardAndRelationWithoutUserOrAccount() {
        when(patientService.getPatientByIdCard(ID_CARD)).thenReturn(null);
        when(patientCardService.insertPatientCard(any(PatientCard.class))).thenReturn(1);
        when(patientService.insertPatient(any(Patient.class))).thenReturn(1);
        when(userPatientRelationService.insertUserPatientRelation(any(UserPatientRelation.class))).thenReturn(1);

        Result<Integer> result = userPatientRelationManager.insertUserPatientRelation(
                CURRENT_USER_ID, "张三", LOWER_ID_CARD, PHONE, "父亲", "家属", "0");

        assertEquals(200, result.getCode());
        ArgumentCaptor<PatientCard> cardCaptor = ArgumentCaptor.forClass(PatientCard.class);
        ArgumentCaptor<Patient> patientCaptor = ArgumentCaptor.forClass(Patient.class);
        ArgumentCaptor<UserPatientRelation> relationCaptor =
                ArgumentCaptor.forClass(UserPatientRelation.class);
        verify(patientCardService).insertPatientCard(cardCaptor.capture());
        verify(patientService).insertPatient(patientCaptor.capture());
        verify(userPatientRelationService).insertUserPatientRelation(relationCaptor.capture());

        Patient patient = patientCaptor.getValue();
        assertEquals("张三", patient.getName());
        assertEquals(ID_CARD, patient.getIdCard());
        assertEquals(PHONE, patient.getPhone());
        assertEquals(CURRENT_USER_ID, patient.getCreatedByUserId());
        assertEquals(cardCaptor.getValue().getId(), patient.getPatientCardId());
        assertNull(patient.getOwnerUserId());
        assertEquals(patient.getId(), relationCaptor.getValue().getPatientId());
        verifyNoInteractions(userService, accountService);
    }

    @Test
    void insertFamilyReusesPatientByNormalizedIdCard() {
        Patient patient = patient(PATIENT_ID, ID_CARD, PHONE);
        when(patientService.getPatientByIdCard(ID_CARD)).thenReturn(patient);
        when(userPatientRelationService.insertUserPatientRelation(any(UserPatientRelation.class))).thenReturn(1);

        Result<Integer> result = userPatientRelationManager.insertUserPatientRelation(
                CURRENT_USER_ID, "张三", LOWER_ID_CARD, PHONE, "父亲", null, "0");

        assertEquals(200, result.getCode());
        verify(patientService, never()).insertPatient(any());
        verify(patientCardService, never()).insertPatientCard(any());
        ArgumentCaptor<UserPatientRelation> captor = ArgumentCaptor.forClass(UserPatientRelation.class);
        verify(userPatientRelationService).insertUserPatientRelation(captor.capture());
        assertEquals(PATIENT_ID, captor.getValue().getPatientId());
        verifyNoInteractions(userService, accountService);
    }

    @Test
    void selfBindingUsesEnabledPatientRoleAccountAndMirrorsUserIdentity() {
        Account account = enabledAccount(PHONE);
        User user = new User();
        user.setId(CURRENT_USER_ID);
        Patient patient = patient(PATIENT_ID, ID_CARD, PHONE);
        patient.setSex(1);

        when(accountService.listPatientAccountsByUserId(CURRENT_USER_ID)).thenReturn(List.of(account));
        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(user);
        when(patientService.getPatientByOwnerUserId(CURRENT_USER_ID)).thenReturn(null);
        when(patientService.getPatientByIdCard(ID_CARD)).thenReturn(patient);
        when(userPatientRelationService.insertUserPatientRelation(any(UserPatientRelation.class))).thenReturn(1);

        Result<Integer> result = userPatientRelationManager.insertUserPatientRelation(
                CURRENT_USER_ID, "张三", LOWER_ID_CARD, PHONE, "本人", null, "0");

        assertEquals(200, result.getCode());
        assertEquals(CURRENT_USER_ID, patient.getOwnerUserId());
        assertEquals(ID_CARD, user.getIdCard());
        assertEquals(1, user.getSex());
        verify(patientService).updatePatientById(patient);
        verify(userService).updateUserById(user);
        verify(userService, never()).getUserByIdCard(any());
        verify(accountService, never()).getAccountByUserId(anyLong());
    }

    @Test
    void selfBindingRejectsMissingEnabledPatientAccount() {
        Account disabled = enabledAccount(PHONE);
        disabled.setEnabled(false);
        when(accountService.listPatientAccountsByUserId(CURRENT_USER_ID)).thenReturn(List.of(disabled));

        Result<Integer> result = userPatientRelationManager.insertUserPatientRelation(
                CURRENT_USER_ID, "张三", ID_CARD, PHONE, "本人", null, "0");

        assertEquals(500, result.getCode());
        assertEquals("当前账号缺少可用的患者角色账号", result.getMessage());
        verifyNoInteractions(patientService, userService);
    }

    @Test
    void selfBindingRejectsMultipleEnabledPatientAccounts() {
        when(accountService.listPatientAccountsByUserId(CURRENT_USER_ID))
                .thenReturn(List.of(enabledAccount(PHONE), enabledAccount(PHONE)));

        Result<Integer> result = userPatientRelationManager.insertUserPatientRelation(
                CURRENT_USER_ID, "张三", ID_CARD, PHONE, "本人", null, "0");

        assertEquals(500, result.getCode());
        assertEquals("当前用户存在多个有效患者角色账号", result.getMessage());
        verifyNoInteractions(patientService, userService);
    }

    @Test
    void selfBindingRejectsPhoneThatDoesNotMatchPatientAccount() {
        when(accountService.listPatientAccountsByUserId(CURRENT_USER_ID))
                .thenReturn(List.of(enabledAccount("13900000000")));

        Result<Integer> result = userPatientRelationManager.insertUserPatientRelation(
                CURRENT_USER_ID, "张三", ID_CARD, PHONE, "本人", null, "0");

        assertEquals(500, result.getCode());
        assertEquals("本人关系手机号需与当前账号一致", result.getMessage());
        verifyNoInteractions(patientService, userService);
    }

    @Test
    void selfBindingRejectsPatientOwnedByOtherUser() {
        User user = new User();
        user.setId(CURRENT_USER_ID);
        Patient patient = patient(PATIENT_ID, ID_CARD, PHONE);
        patient.setOwnerUserId(999L);

        when(accountService.listPatientAccountsByUserId(CURRENT_USER_ID))
                .thenReturn(List.of(enabledAccount(PHONE)));
        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(user);
        when(patientService.getPatientByOwnerUserId(CURRENT_USER_ID)).thenReturn(null);
        when(patientService.getPatientByIdCard(ID_CARD)).thenReturn(patient);

        Result<Integer> result = userPatientRelationManager.insertUserPatientRelation(
                CURRENT_USER_ID, "张三", ID_CARD, PHONE, "本人", null, "0");

        assertEquals(500, result.getCode());
        assertEquals("该身份证已被其他账号绑定", result.getMessage());
        verify(patientService, never()).updatePatientById(any());
    }

    @Test
    void selfBindingRejectsSecondOwnedPatient() {
        Patient owned = patient(999L, "110105194912310011", PHONE);
        when(accountService.listPatientAccountsByUserId(CURRENT_USER_ID))
                .thenReturn(List.of(enabledAccount(PHONE)));
        when(patientService.getPatientByOwnerUserId(CURRENT_USER_ID)).thenReturn(owned);

        Result<Integer> result = userPatientRelationManager.insertUserPatientRelation(
                CURRENT_USER_ID, "张三", ID_CARD, PHONE, "本人", null, "0");

        assertEquals(500, result.getCode());
        assertEquals("当前账号已绑定其他本人患者", result.getMessage());
        verify(patientService, never()).getPatientByIdCard(any());
    }

    @Test
    void updateFamilyRelationCannotModifySharedPatientData() {
        UserPatientRelation relation = relation(RELATION_ID, CURRENT_USER_ID, PATIENT_ID, "父亲");
        Patient patient = patient(PATIENT_ID, ID_CARD, PHONE);
        patient.setCreatedByUserId(999L);
        when(userPatientRelationService.getUserPatientRelationById(RELATION_ID)).thenReturn(relation);
        when(patientService.getPatientById(PATIENT_ID)).thenReturn(patient);

        Result<Integer> result = userPatientRelationManager.updateUserPatientRelationById(
                CURRENT_USER_ID, RELATION_ID, "李四", "13900000000", LOWER_ID_CARD,
                "父亲", "新备注", "0");

        assertEquals(500, result.getCode());
        assertEquals("无权修改患者身份资料", result.getMessage());
        verify(patientService, never()).updatePatientById(any());
        verifyNoInteractions(userService, accountService);
    }

    @Test
    void updateUnclaimedPatientByCreatorUpdatesPatientData() {
        UserPatientRelation relation = relation(RELATION_ID, CURRENT_USER_ID, PATIENT_ID, "父亲");
        Patient patient = patient(PATIENT_ID, ID_CARD, PHONE);
        patient.setCreatedByUserId(CURRENT_USER_ID);
        when(userPatientRelationService.getUserPatientRelationById(RELATION_ID)).thenReturn(relation);
        when(patientService.getPatientById(PATIENT_ID)).thenReturn(patient);
        when(userPatientRelationService.getUserPatientRelationsByUserId(CURRENT_USER_ID))
                .thenReturn(List.of(relation));
        when(userPatientRelationService.updateUserPatientRelationById(relation)).thenReturn(1);

        Result<Integer> result = userPatientRelationManager.updateUserPatientRelationById(
                CURRENT_USER_ID, RELATION_ID, "李四", "13900000000", LOWER_ID_CARD,
                "父亲", "新备注", "0");

        assertEquals(200, result.getCode());
        assertEquals("李四", patient.getName());
        assertEquals("13900000000", patient.getPhone());
        assertEquals(ID_CARD, patient.getIdCard());
        verify(patientService).updatePatientById(patient);
        verifyNoInteractions(userService, accountService);
    }

    @Test
    void insertFamilyConcurrentPatientConflictReusesExistingPatient() {
        Patient existing = patient(PATIENT_ID, ID_CARD, PHONE);
        when(patientService.getPatientByIdCard(ID_CARD)).thenReturn(null);
        when(patientService.getPatientByIdCardForUpdate(ID_CARD)).thenReturn(existing);
        doThrow(new DuplicateKeyException("duplicate id_card"))
                .when(patientService).insertPatient(any(Patient.class));
        when(userPatientRelationService.insertUserPatientRelation(any(UserPatientRelation.class))).thenReturn(1);

        Result<Integer> result = userPatientRelationManager.insertUserPatientRelation(
                CURRENT_USER_ID, "张三", ID_CARD, PHONE, "父亲", null, "0");

        assertEquals(200, result.getCode());
        ArgumentCaptor<UserPatientRelation> relationCaptor =
                ArgumentCaptor.forClass(UserPatientRelation.class);
        verify(userPatientRelationService).insertUserPatientRelation(relationCaptor.capture());
        assertEquals(PATIENT_ID, relationCaptor.getValue().getPatientId());
        verify(patientCardService, never()).insertPatientCard(any());
    }

    @Test
    void insertFamilyDuplicateRelation_rollsBackWithBusinessException() {
        Patient patient = patient(PATIENT_ID, ID_CARD, PHONE);
        when(patientService.getPatientByIdCard(ID_CARD)).thenReturn(patient);
        doThrow(new DuplicateKeyException("duplicate relation"))
                .when(userPatientRelationService).insertUserPatientRelation(any(UserPatientRelation.class));

        BizException ex = assertThrows(BizException.class,
                () -> userPatientRelationManager.insertUserPatientRelation(
                        CURRENT_USER_ID, "张三", ID_CARD, PHONE, "父亲", null, "0"));

        assertEquals(9005, ex.getCode());
    }

    @Test
    void deleteFamilyRelationDoesNotTouchUserOrAccount() {
        UserPatientRelation relation = relation(RELATION_ID, CURRENT_USER_ID, PATIENT_ID, "父亲");
        when(userPatientRelationService.getUserPatientRelationById(RELATION_ID)).thenReturn(relation);
        when(patientService.getPatientById(PATIENT_ID)).thenReturn(patient(PATIENT_ID, ID_CARD, PHONE));
        when(registrationService.listRegistrationsByPatientId(PATIENT_ID)).thenReturn(List.of());
        when(userPatientRelationService.deleteUserPatientRelationById(RELATION_ID)).thenReturn(1);

        Result<Integer> result = userPatientRelationManager.deleteUserPatientRelationById(
                CURRENT_USER_ID, RELATION_ID);

        assertEquals(200, result.getCode());
        verify(userPatientRelationService).deleteUserPatientRelationById(RELATION_ID);
        verify(patientService, never()).updatePatientById(any());
        verifyNoInteractions(userService, accountService);
    }

    @Test
    void deleteSelfRelationIsRejected() {
        UserPatientRelation relation = relation(RELATION_ID, CURRENT_USER_ID, PATIENT_ID, "本人");
        Patient patient = patient(PATIENT_ID, ID_CARD, PHONE);
        patient.setOwnerUserId(CURRENT_USER_ID);
        when(userPatientRelationService.getUserPatientRelationById(RELATION_ID)).thenReturn(relation);
        when(patientService.getPatientById(PATIENT_ID)).thenReturn(patient);

        Result<Integer> result = userPatientRelationManager.deleteUserPatientRelationById(
                CURRENT_USER_ID, RELATION_ID);

        assertEquals(500, result.getCode());
        assertEquals("本人关系不能删除", result.getMessage());
        verify(userPatientRelationService, never()).deleteUserPatientRelationById(anyLong());
    }

    private Account enabledAccount(String phone) {
        Account account = new Account();
        account.setUserId(CURRENT_USER_ID);
        account.setPhone(phone);
        account.setRoleId(4L);
        account.setEnabled(true);
        return account;
    }

    private Patient patient(Long id, String idCard, String phone) {
        Patient patient = new Patient();
        patient.setId(id);
        patient.setName("张三");
        patient.setIdCard(idCard);
        patient.setPhone(phone);
        return patient;
    }

    private UserPatientRelation relation(Long id, Long userId, Long patientId, String relation) {
        UserPatientRelation userPatientRelation = new UserPatientRelation();
        userPatientRelation.setId(id);
        userPatientRelation.setUserId(userId);
        userPatientRelation.setPatientId(patientId);
        userPatientRelation.setRelation(relation);
        userPatientRelation.setDefaulted(false);
        return userPatientRelation;
    }
}
