package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.UserPatientRelation;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.PatientCardService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.JwtTokenRevoker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 添加就诊人单测
 * @Author: YiRanCrazy@gmail.com
 * @Description: 验证本人绑定时的账号归属与归档身份证复用逻辑
 * @Datetime: 2026-10-03 12:00
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

    @Mock
    private JwtTokenRevoker jwtTokenRevoker;

    @InjectMocks
    private UserPatientRelationManager userPatientRelationManager;

    private static final Long CURRENT_USER_ID = 100L;

    private static final Long ARCHIVED_USER_ID = 200L;

    private static final String ID_CARD = "110101199001011234";

    private static final String PHONE = "17330668339";

    @Test
    void insertUserPatientRelation_selfBindingReusesIdCardOfArchivedAccount() {
        User currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);

        Account currentAccount = new Account();
        currentAccount.setUserId(CURRENT_USER_ID);
        currentAccount.setPhone(PHONE);
        currentAccount.setEnabled(true);

        User archivedUser = new User();
        archivedUser.setId(ARCHIVED_USER_ID);
        archivedUser.setIdCard(ID_CARD);

        Account archivedAccount = new Account();
        archivedAccount.setUserId(ARCHIVED_USER_ID);
        archivedAccount.setEnabled(false);

        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(currentUser);
        when(accountService.getAccountByUserId(CURRENT_USER_ID)).thenReturn(currentAccount);
        when(userService.getUserByIdCard(ID_CARD)).thenReturn(archivedUser);
        when(accountService.getAccountByUserId(ARCHIVED_USER_ID)).thenReturn(archivedAccount);
        when(userPatientRelationService.getUserPatientRelationsByUserId(CURRENT_USER_ID))
                .thenReturn(List.of());
        when(userPatientRelationService.insertUserPatientRelation(any(UserPatientRelation.class)))
                .thenReturn(1);

        Result<Integer> result = userPatientRelationManager.insertUserPatientRelation(
                CURRENT_USER_ID, "张三", ID_CARD, PHONE, "本人", "", "0");

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).updateUserById(userCaptor.capture());
        assertEquals(ID_CARD, userCaptor.getValue().getIdCard());
        assertEquals("张三", userCaptor.getValue().getUsername());
    }

    @Test
    void insertUserPatientRelation_selfBindingRejectsPhoneOfAnotherAccount() {
        User currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);

        Account currentAccount = new Account();
        currentAccount.setUserId(CURRENT_USER_ID);
        currentAccount.setPhone(PHONE);
        currentAccount.setEnabled(true);

        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(currentUser);
        when(accountService.getAccountByUserId(CURRENT_USER_ID)).thenReturn(currentAccount);

        Result<Integer> result = userPatientRelationManager.insertUserPatientRelation(
                CURRENT_USER_ID, "张三", ID_CARD, "13900000000", "本人", "", "0");

        assertEquals(500, result.getCode());
        assertEquals("本人关系手机号需与当前账号一致", result.getMessage());
    }

    @Test
    void insertUserPatientRelation_selfBindingRejectsIdCardOfEnabledAccount() {
        User currentUser = new User();
        currentUser.setId(CURRENT_USER_ID);

        Account currentAccount = new Account();
        currentAccount.setUserId(CURRENT_USER_ID);
        currentAccount.setPhone(PHONE);
        currentAccount.setEnabled(true);

        User otherUser = new User();
        otherUser.setId(300L);
        otherUser.setIdCard(ID_CARD);

        Account otherAccount = new Account();
        otherAccount.setUserId(300L);
        otherAccount.setEnabled(true);

        when(userService.getUserById(CURRENT_USER_ID)).thenReturn(currentUser);
        when(accountService.getAccountByUserId(CURRENT_USER_ID)).thenReturn(currentAccount);
        when(userService.getUserByIdCard(ID_CARD)).thenReturn(otherUser);
        when(accountService.getAccountByUserId(300L)).thenReturn(otherAccount);

        Result<Integer> result = userPatientRelationManager.insertUserPatientRelation(
                CURRENT_USER_ID, "张三", ID_CARD, PHONE, "本人", "", "0");

        assertEquals(500, result.getCode());
        assertEquals("该身份证已被其他账号绑定", result.getMessage());
    }
}
