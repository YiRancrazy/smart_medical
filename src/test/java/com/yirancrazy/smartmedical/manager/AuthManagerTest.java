package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.vo.LoginVo;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.PatientCardService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthManager 单测
 * 覆盖：login happy path / 账号不存在 / 密码错误 / logout / register 已存在 / register 成功
 */
@ExtendWith(MockitoExtension.class)
class AuthManagerTest {

    @Mock private AccountService accountService;
    @Mock private UserService userService;
    @Mock private RedisUtil redisUtil;
    @Mock private PatientCardService patientCardService;
    @Mock private PatientService patientService;
    @Mock private HttpServletResponse response;

    @InjectMocks
    private AuthManager authManager;

    private AuthManager buildManager() {
        AuthManager m = new AuthManager(accountService, userService, redisUtil, patientCardService, patientService);
        ReflectionTestUtils.setField(m, "accessSecretKey", "test-access-secret");
        ReflectionTestUtils.setField(m, "refreshSecretKey", "test-refresh-secret");
        ReflectionTestUtils.setField(m, "accessTokenPrefix", "access_token_");
        ReflectionTestUtils.setField(m, "adminRefreshTokenPrefix", "refresh_token_");
        return m;
    }

    @Test
    void login_writesTokensAndReturnsLoginVo() {
        AuthManager m = buildManager();
        User user = new User();
        user.setId(7L);

        Account account = new Account();
        account.setId(42L);
        account.setUserId(7L);
        account.setPhone("13800000000");
        account.setPassword(new BCryptPasswordEncoder().encode("raw"));
        account.setRoleId(4L);

        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of(account));
        when(userService.getUserById(7L)).thenReturn(user);
        lenient().doNothing().when(redisUtil).setEx(anyString(), anyString(), anyLong(), any());

        Result<LoginVo> result = m.login("13800000000", "raw", response);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("42", result.getData().getAccountId());
        assertEquals("7", result.getData().getUid());
        verify(response).setHeader(eq("Authorization"), anyString());
    }

    @Test
    void login_unknownAccount_returnsFail() {
        AuthManager m = buildManager();
        when(accountService.getAccountByPhone("none")).thenReturn(List.of());

        Result<LoginVo> result = m.login("none", "x", response);

        assertEquals(10001, result.getCode());
    }

    @Test
    void login_wrongPassword_returnsFail() {
        AuthManager m = buildManager();
        Account account = new Account();
        account.setId(1L);
        account.setPassword(new BCryptPasswordEncoder().encode("correct"));
        account.setRoleId(4L);
        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of(account));

        Result<LoginVo> result = m.login("13800000000", "wrong", response);

        assertEquals(10002, result.getCode());
    }

    @Test
    void logout_deletesBothTokens() {
        AuthManager m = buildManager();
        when(redisUtil.delete(anyString())).thenReturn(true);

        Result<String> result = m.logout(42L);

        assertEquals(200, result.getCode());
        verify(redisUtil).delete("access_token_42");
        verify(redisUtil).delete("refresh_token_42");
    }

    @Test
    void register_phoneAlreadyExists_returnsFail() {
        AuthManager m = buildManager();
        Account existing = new Account();
        existing.setPhone("13800000000");
        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of(existing));

        Result<String> result = m.register("13800000000", "raw123");

        assertEquals(10001, result.getCode());
    }

    @Test
    void register_newPhone_insertsUserAndAccount() {
        AuthManager m = buildManager();
        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of());
        when(patientCardService.insertPatientCard(any(PatientCard.class))).thenReturn(1);
        when(patientService.insertPatient(any(Patient.class))).thenReturn(1);

        Result<String> result = m.register("13800000000", "raw123");

        assertEquals(200, result.getCode());
        verify(userService).insertUser(any(User.class));
        verify(accountService).insertAccount(any(Account.class));
        verify(patientCardService).insertPatientCard(any(PatientCard.class));
        verify(patientService).insertPatient(any(Patient.class));
    }
}
