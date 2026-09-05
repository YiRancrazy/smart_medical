package com.yirancrazy.smartmedical.manager;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Admin;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AccountDetailResponse;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.AdminService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.RoleService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import com.yirancrazy.smartmedical.pojo.dto.admin.request.AccountUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * AccountManager 单测
 * 覆盖：listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage 各分支
 * @Author: YiRanCrazy@gmail.com
 * @Description: AccountManager 单测
 * @Datetime: 2026-09-01 22:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class AccountManagerTest {

    @Mock private AccountService accountService;
    @Mock private AdminService adminService;
    @Mock private UserService userService;
    @Mock private RoleService roleService;
    @Mock private DoctorService doctorService;
    @Mock private RedisUtil redisUtil;

    @InjectMocks
    private AccountManager accountManager;

    private void setTokenPrefixes() {
        ReflectionTestUtils.setField(accountManager, "accessTokenPrefix", "access_token_");
        ReflectionTestUtils.setField(accountManager, "adminRefreshTokenPrefix", "refresh_token_");
    }

    /**
     * 空列表：返回空 PageInfo
     */
    @Test
    void listAccountDetailResponse_emptyList_returnsEmpty() {
        when(accountService.listAllAccountsByRoleIdAndEnabledAndPage(null, null, null, 1, 10))
                .thenReturn(new PageInfo<>(List.of()));

        Result<PageInfo<AccountDetailResponse>> result = accountManager
                .listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(null, null, null, 1, 10);

        assertEquals(200, result.getCode());
        assertTrue(result.getData().getList().isEmpty());
    }

    /**
     * 管理员角色(roleId=1)：加载 admin 实体，username 映射正确
     */
    @SuppressWarnings("unchecked")
    @Test
    void listAccountDetailResponse_adminRole_mapsAdminName() {
        Account account = new Account();
        account.setId(1001L);
        account.setUserId(5001L);
        account.setPhone("13800138000");
        account.setRoleId(1L);
        account.setEnabled(true);

        Admin admin = new Admin();
        admin.setId(5001L);
        admin.setName("系统管理员");

        PageInfo<Account> pageInfo = new PageInfo<>(List.of(account));
        when(accountService.listAllAccountsByRoleIdAndEnabledAndPage(null, 1L, null, 1, 10))
                .thenReturn(pageInfo);
        when(adminService.listAdminsByIds(List.of(5001L))).thenReturn(List.of(admin));

        Result<PageInfo<AccountDetailResponse>> result = accountManager
                .listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(null, 1L, null, 1, 10);

        assertEquals(200, result.getCode());
        List<AccountDetailResponse> list = result.getData().getList();
        assertEquals(1, list.size());
        AccountDetailResponse resp = list.get(0);
        assertEquals("1001", resp.getId());
        assertEquals(5001L, resp.getUserId());
        assertEquals("13800138000", resp.getPhone());
        assertEquals(1L, resp.getRoleId());
        assertEquals("系统管理员", resp.getUsername());
    }

    /**
     * 医生角色(roleId=2)：加载 doctor 实体，username 映射正确
     */
    @SuppressWarnings("unchecked")
    @Test
    void listAccountDetailResponse_doctorRole_mapsDoctorName() {
        Account account = new Account();
        account.setId(1002L);
        account.setUserId(6001L);
        account.setPhone("13900139000");
        account.setRoleId(2L);
        account.setEnabled(true);

        Doctor doctor = new Doctor();
        doctor.setId(6001L);
        doctor.setName("李医生");

        PageInfo<Account> pageInfo = new PageInfo<>(List.of(account));
        when(accountService.listAllAccountsByRoleIdAndEnabledAndPage(null, 2L, null, 1, 10))
                .thenReturn(pageInfo);
        when(doctorService.listDoctorsByIds(List.of(6001L))).thenReturn(List.of(doctor));

        Result<PageInfo<AccountDetailResponse>> result = accountManager
                .listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(null, 2L, null, 1, 10);

        List<AccountDetailResponse> list = result.getData().getList();
        assertEquals(1, list.size());
        assertEquals("李医生", list.get(0).getUsername());
    }

    /**
     * roleId=null 混合角色：按账户实际 roleId 分别加载 admin/doctor
     */
    @SuppressWarnings("unchecked")
    @Test
    void listAccountDetailResponse_nullRoleId_loadsMixedEntities() {
        Account adminAcc = new Account();
        adminAcc.setId(2001L);
        adminAcc.setUserId(7001L);
        adminAcc.setRoleId(1L);

        Account doctorAcc = new Account();
        doctorAcc.setId(2002L);
        doctorAcc.setUserId(8001L);
        doctorAcc.setRoleId(2L);

        Admin admin = new Admin();
        admin.setId(7001L);
        admin.setName("管理员");

        Doctor doctor = new Doctor();
        doctor.setId(8001L);
        doctor.setName("张医生");

        PageInfo<Account> pageInfo = new PageInfo<>(List.of(adminAcc, doctorAcc));
        when(accountService.listAllAccountsByRoleIdAndEnabledAndPage(null, null, null, 1, 10))
                .thenReturn(pageInfo);
        when(adminService.listAdminsByIds(List.of(7001L))).thenReturn(List.of(admin));
        when(doctorService.listDoctorsByIds(List.of(8001L))).thenReturn(List.of(doctor));

        Result<PageInfo<AccountDetailResponse>> result = accountManager
                .listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(null, null, null, 1, 10);

        List<AccountDetailResponse> list = result.getData().getList();
        assertEquals(2, list.size());
        assertEquals("管理员", list.get(0).getUsername());
        assertEquals("张医生", list.get(1).getUsername());
    }

    /**
     * 删除账户后吊销 access/refresh token
     */
    @Test
    void deleteAccount_revokesTokens() {
        setTokenPrefixes();
        Account account = new Account();
        account.setId(1001L);
        when(accountService.getAccountById(1001L)).thenReturn(account);

        accountManager.deleteAccount(1001L);

        org.mockito.Mockito.verify(redisUtil).delete("access_token_1001");
        org.mockito.Mockito.verify(redisUtil).delete("refresh_token_1001");
    }

    /**
     * 角色变更后吊销旧 token
     */
    @Test
    void updateAccount_roleChange_revokesTokens() {
        setTokenPrefixes();
        Account account = new Account();
        account.setId(1001L);
        when(accountService.getAccountById(1001L)).thenReturn(account);

        AccountUpdateRequest request = new AccountUpdateRequest();
        request.setRoleId(2L);
        accountManager.updateAccount(1001L, request);

        org.mockito.Mockito.verify(redisUtil).delete("access_token_1001");
        org.mockito.Mockito.verify(redisUtil).delete("refresh_token_1001");
    }
}