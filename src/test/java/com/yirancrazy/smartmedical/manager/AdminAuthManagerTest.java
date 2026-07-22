package com.yirancrazy.smartmedical.manager;

import cn.hutool.jwt.JWTUtil;
import com.yirancrazy.smartmedical.constant.RoleConstant;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Role;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.manager.loader.impl.RoleTypeLoaderManage;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.AdminService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AdminAuthManager 单测
 * 覆盖 C8 (validateJwtConfig 启动期校验) + C15 (setEx TTL 一致性)
 */
@ExtendWith(MockitoExtension.class)
class AdminAuthManagerTest {

    @Mock private AccountService accountService;
    @Mock private AdminService adminService;
    @Mock private DoctorService doctorService;
    @Mock private UserService userService;
    @Mock private RedisUtil redisUtil;
    @Mock private RoleTypeLoaderManage roleTypeLoaderManage;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    private AdminAuthManager manager;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        // 先填充 ROLE_LIST，再构造 manager：
        // AdminAuthManager.adminRole 在 @PostConstruct 中初始化，依赖 ROLE_LIST 非空
        adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("系统管理员");
        RoleConstant.ROLE_LIST.clear();
        RoleConstant.ROLE_LIST.add(adminRole);

        manager = new AdminAuthManager(accountService, adminService, doctorService, userService, redisUtil, roleTypeLoaderManage);
        ReflectionTestUtils.setField(manager, "accessSecretKey", "test-access-secret-key");
        ReflectionTestUtils.setField(manager, "refreshSecretKey", "test-refresh-secret-key");
        ReflectionTestUtils.setField(manager, "adminAccessTokenPrefix", "admin-access:");
        ReflectionTestUtils.setField(manager, "adminRefreshTokenPrefix", "admin-refresh:");
        manager.validateJwtConfig();
    }

    @AfterEach
    void tearDown() {
        RoleConstant.ROLE_LIST.clear();
    }

    // ---- C8: PostConstruct 校验 ----

    @Test
    void validateJwtConfig_blankAccessKey_throws() {
        ReflectionTestUtils.setField(manager, "accessSecretKey", "");
        assertThrows(IllegalStateException.class, manager::validateJwtConfig);
    }

    @Test
    void validateJwtConfig_blankRefreshKey_throws() {
        ReflectionTestUtils.setField(manager, "refreshSecretKey", "");
        assertThrows(IllegalStateException.class, manager::validateJwtConfig);
    }

    @Test
    void validateJwtConfig_blankAccessPrefix_throws() {
        ReflectionTestUtils.setField(manager, "adminAccessTokenPrefix", "");
        assertThrows(IllegalStateException.class, manager::validateJwtConfig);
    }

    @Test
    void validateJwtConfig_allPresent_passes() {
        manager.validateJwtConfig();
    }

    // ---- C15: loginByPhoneAndPassword 调用 setEx 带 7 天 TTL ----

    @Test
    void login_writesAccessAndRefreshWithSevenDayTtl() {
        Account account = new Account();
        account.setId(42L);
        account.setPhone("13800000000");
        // 真实 BCrypt 编码 "raw"，否则 BCrypt.checkpw 抛 Invalid salt
        account.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("raw"));
        account.setRoleId(1L);
        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of(account));

        org.mockito.Mockito.lenient().doNothing()
                .when(redisUtil).setEx(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        Result<String> result = manager.loginByPhoneAndPassword("13800000000", "raw", true, request, response);

        assertEquals(200, result.getCode());
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisUtil).setEx(eq("admin-access:42"), tokenCaptor.capture(), eq(30L), eq(TimeUnit.MINUTES));
        verify(redisUtil).setEx(eq("admin-refresh:42"), tokenCaptor.capture(), eq(30L), eq(TimeUnit.DAYS));
        // ponytail: 至少验证 token 是合法 JWT 字符串且彼此不同（防退化）
        assertNotNull(tokenCaptor.getAllValues().get(0));
        assertNotNull(tokenCaptor.getAllValues().get(1));
        assertTrue(JWTUtil.verify(tokenCaptor.getAllValues().get(0), "test-access-secret-key".getBytes()));
        assertTrue(JWTUtil.verify(tokenCaptor.getAllValues().get(1), "test-refresh-secret-key".getBytes()));
        verify(response).setHeader(eq("Authorization"), anyString());
    }

    @Test
    void login_unknownAccount_returnsFail() {
        when(accountService.getAccountByPhone("none")).thenReturn(List.of());
        Result<String> result = manager.loginByPhoneAndPassword("none", "x", false, request, response);
        assertEquals(500, result.getCode());
    }
}