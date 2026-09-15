package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.dto.user.request.UpdateUserProfileRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.request.UpdateUserPhoneRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.request.UserPhoneSmsCodeRequest;
import com.yirancrazy.smartmedical.pojo.vo.UserBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.UserInfoVo;
import com.yirancrazy.smartmedical.pojo.vo.UserProfileInfo;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.SmsService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.utils.MinIOUtil;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UserManager 单测
 */
@ExtendWith(MockitoExtension.class)
class UserManagerTest {

    @Mock private UserService userService;
    @Mock private AccountService accountService;
    @Mock private RedisUtil redisUtil;
    @Mock private SmsService smsService;

    @InjectMocks
    private UserManager userManager;

    @Test
    void addUser_assignsSnowflakeIdAndDelegates() {
        when(userService.insertUser(any(User.class))).thenReturn(1);

        int rows = userManager.addUser(new User());

        assertEquals(1, rows);
        verify(userService).insertUser(any(User.class));
    }

    @Test
    void getUserById_delegates() {
        User u = new User();
        u.setId(1L);
        when(userService.getUserById(1L)).thenReturn(u);

        User result = userManager.getUserById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void deleteUserById_delegates() {
        when(userService.deleteUserById(1L)).thenReturn(1);

        int rows = userManager.deleteUserById(1L);

        assertEquals(1, rows);
    }

    @Test
    void getUserDetailsById_assemblesVo() {
        Account account = new Account();
        account.setId(42L);
        account.setUserId(7L);
        account.setEmail("a@b.com");
        account.setPhone("13800000000");
        User user = new User();
        user.setId(7L);
        user.setAvatar("https://example.com/a.jpg");
        user.setSex(1);
        user.setUsername("nick");

        when(accountService.getAccountById(42L)).thenReturn(account);
        when(userService.getUserById(7L)).thenReturn(user);

        Result<UserInfoVo> result = userManager.getUserDetailsById(42L);

        assertEquals(200, result.getCode());
        UserInfoVo vo = result.getData();
        assertNotNull(vo);
        assertEquals("42", vo.getAccountId());
        assertEquals("7", vo.getUserId());
        assertEquals("a@b.com", vo.getEmail());
        assertEquals("13800000000", vo.getPhone());
        assertEquals("https://example.com/a.jpg", vo.getAvatar());
        assertEquals(1, vo.getSex());
        assertEquals("nick", vo.getNickName());
    }

    @Test
    void getUserBaseInfoByUserId_wrapsInSuccess() {
        UserBaseInfo info = new UserBaseInfo();
        when(userService.getUserBaseInfoByUserId(7L)).thenReturn(info);

        Result<UserBaseInfo> result = userManager.getUserBaseInfoByUserId(7L);

        assertEquals(200, result.getCode());
    }

    @Test
    void getUserProfile_marksCompleteOnlyWhenRequiredFieldsPresent() {
        User user = new User();
        user.setId(7L);
        user.setUsername("张三");
        user.setIdCard("340521200203221034");
        user.setSex(0);
        user.setNickname("小张");
        Account account = new Account();
        account.setPhone("13800000000");
        when(userService.getUserById(7L)).thenReturn(user);
        when(accountService.getAccountByUserId(7L)).thenReturn(account);

        Result<UserProfileInfo> result = userManager.getUserProfile(7L);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("小张", result.getData().getNickname());
        assertEquals("13800000000", result.getData().getPhone());
        assertTrue(result.getData().getProfileCompleted());
    }

    @Test
    void getUserProfile_returnsPresignedAvatarUrlForStoredObject() {
        User user = new User();
        user.setId(7L);
        user.setUsername("张三");
        user.setIdCard("340521200203221034");
        user.setSex(0);
        user.setAvatar("user/avatar/a.png");
        Account account = new Account();
        account.setPhone("13800000000");
        when(userService.getUserById(7L)).thenReturn(user);
        when(accountService.getAccountByUserId(7L)).thenReturn(account);

        try (MockedStatic<MinIOUtil> minio = mockStatic(MinIOUtil.class)) {
            minio.when(MinIOUtil::getBucketName).thenReturn("smart-medical");
            minio.when(MinIOUtil::getBasisUrl).thenReturn("http://public/smart-medical/");
            minio.when(() -> MinIOUtil.getPresignedObjectUrl(
                            eq("smart-medical"), eq("user/avatar/a.png"), anyInt()))
                    .thenReturn("http://public/signature");

            Result<UserProfileInfo> result = userManager.getUserProfile(7L);

            assertEquals("http://public/signature", result.getData().getAvatar());
        }
    }

    @Test
    void getUserProfile_marksIncompleteWhenSexMissing() {
        User user = new User();
        user.setId(7L);
        user.setUsername("张三");
        user.setIdCard("340521200203221034");
        when(userService.getUserById(7L)).thenReturn(user);

        Result<UserProfileInfo> result = userManager.getUserProfile(7L);

        assertFalse(result.getData().getProfileCompleted());
    }

    @Test
    void updateUserProfile_trimsInputAndReturnsCompleteProfile() {
        User user = new User();
        user.setId(7L);
        when(userService.getUserById(7L)).thenReturn(user);
        when(userService.updateUserById(any(User.class))).thenReturn(1);

        Result<UserProfileInfo> result = userManager.updateUserProfile(
                7L,
                new UpdateUserProfileRequest(" 张三 ", " 小张 ", "340521200203221034", 1, " 北京市朝阳区 "));

        assertEquals(200, result.getCode());
        assertEquals("张三", result.getData().getUsername());
        assertEquals("小张", result.getData().getNickname());
        assertEquals("小张", user.getNickname());
        assertEquals("北京市朝阳区", result.getData().getAddress());
        assertTrue(result.getData().getProfileCompleted());
        verify(userService).updateUserById(user);
    }

    @Test
    void uploadAvatar_rejectsNonImage() {
        User user = new User();
        user.setId(7L);
        when(userService.getUserById(7L)).thenReturn(user);
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.txt", "text/plain", "not-image".getBytes());

        Result<String> result = userManager.uploadAvatar(7L, file);

        assertEquals(500, result.getCode());
        assertEquals("仅允许上传图片文件", result.getMessage());
    }

    @Test
    void uploadAvatar_uploadsAndPersistsUrl() {
        User user = new User();
        user.setId(7L);
        when(userService.getUserById(7L)).thenReturn(user);
        when(userService.updateUserById(any(User.class))).thenReturn(1);
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", "image/png", "image".getBytes());

        try (MockedStatic<MinIOUtil> minio = mockStatic(MinIOUtil.class)) {
            minio.when(MinIOUtil::getBucketName).thenReturn("smart-medical");
            minio.when(MinIOUtil::getBasisUrl).thenReturn("http://public/smart-medical/");
            minio.when(() -> MinIOUtil.uploadFile(anyString(), any(), anyString(), anyString()))
                    .thenAnswer(invocation -> null);
            minio.when(() -> MinIOUtil.getPresignedObjectUrl(
                            eq("smart-medical"), anyString(), anyInt()))
                    .thenReturn("http://public/signature");

            Result<String> result = userManager.uploadAvatar(7L, file);

            assertEquals(200, result.getCode());
            assertEquals("http://public/signature", result.getData());
            assertTrue(user.getAvatar().startsWith("user/avatar/"));
            verify(userService).updateUserById(user);
        }
    }

    @Test
    void sendPhoneChangeCode_rejectsOldPhoneMismatch() {
        Account account = new Account();
        account.setId(42L);
        account.setPhone("13800000000");
        when(accountService.getAccountById(42L)).thenReturn(account);

        Result<String> result = userManager.sendPhoneChangeCode(
                42L, new UserPhoneSmsCodeRequest("13900000000", "old"));

        assertEquals(500, result.getCode());
        assertEquals("旧手机号与当前账号不一致", result.getMessage());
    }

    @Test
    void sendPhoneChangeCode_rejectsRegisteredNewPhone() {
        Account account = new Account();
        account.setId(42L);
        account.setPhone("13800000000");
        when(accountService.getAccountById(42L)).thenReturn(account);
        when(accountService.getAccountByPhone("13900000000")).thenReturn(List.of(new Account()));

        Result<String> result = userManager.sendPhoneChangeCode(
                42L, new UserPhoneSmsCodeRequest("13900000000", "new"));

        assertEquals(500, result.getCode());
        assertEquals("该手机号已注册", result.getMessage());
    }

    @Test
    void sendPhoneChangeCode_rejectsSamePhone() {
        Account account = new Account();
        account.setId(42L);
        account.setPhone("13800000000");
        when(accountService.getAccountById(42L)).thenReturn(account);

        Result<String> result = userManager.sendPhoneChangeCode(
                42L, new UserPhoneSmsCodeRequest("13800000000", "new"));

        assertEquals(500, result.getCode());
        assertEquals("新手机号不能与当前手机号相同", result.getMessage());
    }

    @Test
    void changePhone_verifiesBothCodesAndRevokesTokens() {
        ReflectionTestUtils.setField(userManager, "accessTokenPrefix", "jwt:access:");
        ReflectionTestUtils.setField(userManager, "adminRefreshTokenPrefix", "jwt:refresh:");
        Account account = new Account();
        account.setId(42L);
        account.setPhone("13800000000");
        when(accountService.getAccountById(42L)).thenReturn(account);
        when(accountService.getAccountByPhone("13900000000")).thenReturn(List.of());
        when(accountService.updateAccountById(account)).thenReturn(1);

        Result<String> result = userManager.changePhone(
                42L, new UpdateUserPhoneRequest("123456", "13900000000", "654321"));

        assertEquals(200, result.getCode());
        assertEquals("13900000000", account.getPhone());
        verify(smsService).verifyCode("13800000000", "123456");
        verify(smsService).verifyCode("13900000000", "654321");
        verify(redisUtil).delete("jwt:access:42");
        verify(redisUtil).delete("jwt:refresh:42");
        verify(accountService).updateAccountById(account);
    }

    @Test
    void changePhone_rejectsWrongOldPhoneCode() {
        Account account = new Account();
        account.setId(42L);
        account.setPhone("13800000000");
        when(accountService.getAccountById(42L)).thenReturn(account);
        doThrow(new BizException(BizErrorCode.SMS_CODE_WRONG))
                .when(smsService).verifyCode("13800000000", "000000");

        assertThrows(BizException.class, () -> userManager.changePhone(
                42L, new UpdateUserPhoneRequest("000000", "13900000000", "654321")));
    }

    @Test
    void changePhone_rejectsWrongNewPhoneCode() {
        Account account = new Account();
        account.setId(42L);
        account.setPhone("13800000000");
        when(accountService.getAccountById(42L)).thenReturn(account);
        when(accountService.getAccountByPhone("13900000000")).thenReturn(List.of());
        doNothing().when(smsService).verifyCode("13800000000", "123456");
        doThrow(new BizException(BizErrorCode.SMS_CODE_WRONG))
                .when(smsService).verifyCode("13900000000", "000000");

        assertThrows(BizException.class, () -> userManager.changePhone(
                42L, new UpdateUserPhoneRequest("123456", "13900000000", "000000")));
    }

    @Test
    void changePhone_rejectsSamePhone() {
        Account account = new Account();
        account.setId(42L);
        account.setPhone("13800000000");
        when(accountService.getAccountById(42L)).thenReturn(account);

        Result<String> result = userManager.changePhone(
                42L, new UpdateUserPhoneRequest("123456", "13800000000", "654321"));

        assertEquals(500, result.getCode());
        assertEquals("新手机号不能与当前手机号相同", result.getMessage());
    }

    @Test
    void changePhone_convertsUniqueIndexRaceToBusinessFailure() {
        Account account = new Account();
        account.setId(42L);
        account.setPhone("13800000000");
        when(accountService.getAccountById(42L)).thenReturn(account);
        when(accountService.getAccountByPhone("13900000000")).thenReturn(List.of());
        when(accountService.updateAccountById(account))
                .thenThrow(new DuplicateKeyException("duplicate"));

        Result<String> result = userManager.changePhone(
                42L, new UpdateUserPhoneRequest("123456", "13900000000", "654321"));

        assertEquals(500, result.getCode());
        assertEquals("该手机号已注册", result.getMessage());
    }
}
