package com.yirancrazy.smartmedical.manager;

import cn.hutool.jwt.JWTUtil;
import com.yirancrazy.smartmedical.constant.RoleConstant;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Role;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.AdminService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.RoleService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.JwtTokenRevoker;
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
import static org.mockito.Mockito.never;
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
    @Mock private RoleService roleService;
    @Mock private RedisUtil redisUtil;
    @Mock private JwtTokenRevoker jwtTokenRevoker;
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

        manager = new AdminAuthManager(accountService, adminService, doctorService, userService, roleService,
                redisUtil, jwtTokenRevoker);
        ReflectionTestUtils.setField(manager, "accessSecretKey", "test-access-secret-key");
        ReflectionTestUtils.setField(manager, "refreshSecretKey", "test-refresh-secret-key");
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
    void validateJwtConfig_allPresent_passes() {
        manager.validateJwtConfig();
    }

    // ---- C15: access token 7 天有效且不写 Redis，refresh token 仍写 Redis ----

    @Test
    void login_writesOnlyRefreshAndIssuesSevenDayAccessToken() {
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
        ArgumentCaptor<String> headerCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> refreshCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).setHeader(eq("Authorization"), headerCaptor.capture());
        verify(redisUtil).setEx(eq("admin-refresh:42"), refreshCaptor.capture(), eq(30L), eq(TimeUnit.DAYS));
        verify(redisUtil, never()).setEx(eq("admin-access:42"), anyString(), anyLong(), any(TimeUnit.class));
        verify(jwtTokenRevoker).clear(42L);

        String accessToken = headerCaptor.getValue().substring("Bearer ".length());
        String refreshToken = refreshCaptor.getValue();
        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertTrue(JWTUtil.verify(accessToken, "test-access-secret-key".getBytes()));
        assertTrue(JWTUtil.verify(refreshToken, "test-refresh-secret-key".getBytes()));

        long nowSeconds = System.currentTimeMillis() / 1000;
        long accessExp = Long.parseLong(String.valueOf(
                JWTUtil.parseToken(accessToken).getPayload().getClaim("exp")));
        long accessIatMs = Long.parseLong(String.valueOf(
                JWTUtil.parseToken(accessToken).getPayload().getClaim("iatMs")));
        long refreshExp = Long.parseLong(String.valueOf(
                JWTUtil.parseToken(refreshToken).getPayload().getClaim("exp")));
        assertTrue(Math.abs(accessExp - (nowSeconds + 7L * 24 * 60 * 60)) <= 5);
        // exp 与 iatMs 必须由同一次时间采样派生，差值恰为 7 天
        assertTrue(Math.abs((accessExp - accessIatMs / 1000) - 7L * 24 * 60 * 60) <= 1);
        assertTrue(Math.abs(refreshExp - (nowSeconds + 30L * 24 * 60 * 60)) <= 5);
    }

    @Test
    void login_unknownAccount_returnsFail() {
        when(accountService.getAccountByPhone("none")).thenReturn(List.of());
        Result<String> result = manager.loginByPhoneAndPassword("none", "x", false, request, response);
        assertEquals(500, result.getCode());
    }

    @Test
    void login_rateLimitDisabled_skipsRedisCounter() {
        ReflectionTestUtils.setField(manager, "loginRateEnabled", false);
        Account account = new Account();
        account.setId(42L);
        account.setPhone("13800000000");
        account.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("raw"));
        account.setRoleId(1L);
        when(accountService.getAccountByPhone("13800000000")).thenReturn(List.of(account));
        org.mockito.Mockito.lenient().doNothing()
                .when(redisUtil).setEx(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        Result<String> result = manager.loginByPhoneAndPassword("13800000000", "raw", true, request, response);

        assertEquals(200, result.getCode());
        verify(redisUtil, never()).incrAndExpireOnFirst(
                anyString(), anyLong(), anyLong(), any(TimeUnit.class));
    }
}
