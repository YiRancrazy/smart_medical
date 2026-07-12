package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.vo.UserBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.UserInfoVo;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
        user.setAvatar("a.jpg");
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
        assertEquals("a.jpg", vo.getAvatar());
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
}
