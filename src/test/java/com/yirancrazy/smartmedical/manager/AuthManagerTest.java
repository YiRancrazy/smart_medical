package com.yirancrazy.smartmedical.manager;

import cn.hutool.jwt.JWT;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.PatientCard;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.vo.LoginVo;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.PatientCardService;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.SmsService;
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
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    @Mock private SmsService smsService;
    @Mock private HttpServletResponse response;

    @InjectMocks
    private AuthManager authManager;

    private AuthManager buildManager() {
        AuthManager m = new AuthManager(accountService, userService, redisUtil, patientCardService, patientService, smsService);
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

        Result<LoginVo> result = m.register("13800000000", "raw123", response);

        assertEquals(10001, result.getCode());
        verify(smsService).verifyCode("13800000000", "raw123");
    }

    @Test
    void register_newPhone_insertsUserAndAccount() {
        AuthManager m = buildManager();
        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of());
        when(patientCardService.insertPatientCard(any(PatientCard.class))).thenReturn(1);
        when(patientService.insertPatient(any(Patient.class))).thenReturn(1);

        Result<LoginVo> result = m.register("13800000000", "raw123", response);

        assertEquals(200, result.getCode());
        verify(smsService).verifyCode("13800000000", "raw123");
        verify(userService).insertUser(any(User.class));
        verify(accountService).insertAccount(any(Account.class));
        verify(patientCardService).insertPatientCard(any(PatientCard.class));
        verify(patientService).insertPatient(any(Patient.class));
    }

    @Test
    void forgotPassword_verifyCodeRequiredBeforeReset() {
        AuthManager m = buildManager();
        Account existing = new Account();
        existing.setId(9L);
        existing.setRoleId(4L);
        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of(existing));

        Result<String> result = m.forgotPassword("13800000000", "123456", "newpass123");

        assertEquals(200, result.getCode());
        verify(smsService).verifyCode("13800000000", "123456");
        verify(accountService).updateAccountById(existing);
        verify(redisUtil).delete("access_token_9");
        verify(redisUtil).delete("refresh_token_9");
    }

    @Test
    void login_rateLimitExceeded_throwsBizException() {
        AuthManager m = buildManager();
        when(redisUtil.incrAndExpireOnFirst(
                "login:rate:user:13800000000", 1L, 5L, TimeUnit.MINUTES)).thenReturn(6L);

        BizException exception = assertThrows(BizException.class, () ->
                m.login("13800000000", "raw", response));

        assertEquals(BizErrorCode.LOGIN_RATE_LIMITED.getCode(), exception.getCode());
        verifyNoInteractions(accountService);
    }

    @Test
    void login_rateLimitDisabled_skipsRedisCounter() {
        AuthManager m = buildManager();
        ReflectionTestUtils.setField(m, "loginRateEnabled", false);
        User user = new User();
        user.setId(7L);
        Account account = userAccount(42L, 7L);

        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of(account));
        when(userService.getUserById(7L)).thenReturn(user);
        lenient().doNothing().when(redisUtil).setEx(anyString(), anyString(), anyLong(), any());

        Result<LoginVo> result = m.login("13800000000", "raw", response);

        assertEquals(200, result.getCode());
        verify(redisUtil, never()).incrAndExpireOnFirst(
                anyString(), anyLong(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void loginByCode_validCode_returnsLoginVo() {
        AuthManager m = buildManager();
        User user = new User();
        user.setId(7L);
        user.setNickname("患者");
        Account account = userAccount(42L, 7L);

        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of(account));
        when(userService.getUserById(7L)).thenReturn(user);
        lenient().doNothing().when(redisUtil).setEx(anyString(), anyString(), anyLong(), any());

        Result<LoginVo> result = m.loginByCode("13800000000", "123456", response);

        assertEquals(200, result.getCode());
        assertEquals("42", result.getData().getAccountId());
        assertEquals("患者", result.getData().getUserName());
        verify(smsService).verifyCode("13800000000", "123456");
        verify(response).setHeader(eq("Authorization"), anyString());
    }

    @Test
    void loginByCode_accountMissing_returnsAccountNotFound() {
        AuthManager m = buildManager();
        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of());

        Result<LoginVo> result = m.loginByCode("13800000000", "123456", response);

        assertEquals(10001, result.getCode());
        verify(smsService).verifyCode("13800000000", "123456");
    }

    @Test
    void sendSmsCode_invalidPhone_returnsFailWithoutServiceCalls() {
        AuthManager m = buildManager();

        Result<String> result = m.sendSmsCode("123", "register");

        assertEquals(500, result.getCode());
        assertEquals("手机号格式不正确", result.getMessage());
        verifyNoInteractions(accountService, smsService);
    }

    @Test
    void sendSmsCode_loginMissingAccount_returnsAccountNotFound() {
        AuthManager m = buildManager();
        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of());

        Result<String> result = m.sendSmsCode("13800000000", "login");

        assertEquals(10001, result.getCode());
        verify(smsService, never()).sendCode(anyString());
    }

    @Test
    void sendSmsCode_registerExistingAccount_returnsAccountExists() {
        AuthManager m = buildManager();
        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of(userAccount(42L, 7L)));

        Result<String> result = m.sendSmsCode("13800000000", "register");

        assertEquals(10001, result.getCode());
        assertEquals("账号已存在", result.getMessage());
        verify(smsService, never()).sendCode(anyString());
    }

    @Test
    void sendSmsCode_registerNewPhone_delegatesToSmsService() {
        AuthManager m = buildManager();
        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of());
        when(smsService.sendCode("13800000000")).thenReturn("sent");

        Result<String> result = m.sendSmsCode("13800000000", "register");

        assertEquals(200, result.getCode());
        assertEquals("sent", result.getData());
    }

    @Test
    void changePassword_wrongOldPassword_returnsErrorWithoutUpdate() {
        AuthManager m = buildManager();
        Account account = userAccount(42L, 7L);
        account.setPassword(new BCryptPasswordEncoder().encode("old-password"));
        when(accountService.getAccountById(42L)).thenReturn(account);

        Result<String> result = m.changePassword(42L, "wrong-password", "new-password");

        assertEquals(10002, result.getCode());
        verify(accountService, never()).updateAccountById(any());
        verify(redisUtil, never()).delete(anyString());
    }

    @Test
    void changePassword_validOldPassword_updatesAndInvalidatesTokens() {
        AuthManager m = buildManager();
        Account account = userAccount(42L, 7L);
        account.setPassword(new BCryptPasswordEncoder().encode("old-password"));
        when(accountService.getAccountById(42L)).thenReturn(account);

        Result<String> result = m.changePassword(42L, "old-password", "new-password");

        assertEquals(200, result.getCode());
        verify(accountService).updateAccountById(account);
        verify(redisUtil).delete("access_token_42");
        verify(redisUtil).delete("refresh_token_42");
    }

    @Test
    void refresh_validToken_rotatesAccessToken() {
        AuthManager m = buildManager();
        Account account = userAccount(42L, 7L);
        account.setEnabled(true);
        String refreshToken = buildRefreshToken(42L, 7L, 4L,
                System.currentTimeMillis() / 1000 + 3600);

        when(accountService.getAccountById(42L)).thenReturn(account);
        when(redisUtil.get("refresh_token_42")).thenReturn(refreshToken);

        Result<String> result = m.refresh(refreshToken, response);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(redisUtil).setEx(eq("access_token_42"), eq(result.getData()), eq(30L), eq(TimeUnit.MINUTES));
        verify(response).setHeader(eq("Authorization"), eq("Bearer " + result.getData()));
    }

    @Test
    void refresh_redisTokenMismatch_returnsFail() {
        AuthManager m = buildManager();
        Account account = userAccount(42L, 7L);
        account.setEnabled(true);
        String refreshToken = buildRefreshToken(42L, 7L, 4L,
                System.currentTimeMillis() / 1000 + 3600);

        when(accountService.getAccountById(42L)).thenReturn(account);
        when(redisUtil.get("refresh_token_42")).thenReturn("another-refresh-token");

        Result<String> result = m.refresh(refreshToken, response);

        assertEquals(500, result.getCode());
        assertEquals("Refresh token 已失效", result.getMessage());
        verify(response, never()).setHeader(anyString(), anyString());
    }

    @Test
    void refresh_disabledAccount_invalidatesAllTokens() {
        AuthManager m = buildManager();
        Account account = userAccount(42L, 7L);
        account.setEnabled(false);
        String refreshToken = buildRefreshToken(42L, 7L, 4L,
                System.currentTimeMillis() / 1000 + 3600);

        when(accountService.getAccountById(42L)).thenReturn(account);

        Result<String> result = m.refresh(refreshToken, response);

        assertEquals(500, result.getCode());
        assertEquals("账号已被禁用，请重新登录", result.getMessage());
        verify(redisUtil).delete("refresh_token_42");
        verify(redisUtil).delete("access_token_42");
    }

    @Test
    void refresh_expiredToken_returnsFail() {
        AuthManager m = buildManager();
        Account account = userAccount(42L, 7L);
        account.setEnabled(true);
        String refreshToken = buildRefreshToken(42L, 7L, 4L,
                System.currentTimeMillis() / 1000 - 1);

        when(accountService.getAccountById(42L)).thenReturn(account);

        Result<String> result = m.refresh(refreshToken, response);

        assertEquals(500, result.getCode());
        assertEquals("Refresh token 已过期", result.getMessage());
        verify(redisUtil, never()).get(anyString());
    }

    private Account userAccount(Long accountId, Long userId) {
        Account account = new Account();
        account.setId(accountId);
        account.setUserId(userId);
        account.setPhone("13800000000");
        account.setRoleId(4L);
        account.setEnabled(true);
        account.setPassword(new BCryptPasswordEncoder().encode("raw"));
        return account;
    }

    private String buildRefreshToken(Long accountId, Long userId, Long roleId, long expiresAt) {
        return JWT.create()
                .setPayload("sub", String.valueOf(accountId))
                .setPayload("userId", userId)
                .setPayload("role", roleId)
                .setPayload("exp", expiresAt)
                .setKey("test-refresh-secret".getBytes())
                .sign();
    }
}
