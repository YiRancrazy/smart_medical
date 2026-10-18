package com.yirancrazy.smartmedical.manager;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.constant.type.RoleEnum;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Admin;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.common.PageResult;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.AccountDetailResponse;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.AdminService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.JwtTokenRevoker;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
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
    @Mock private DoctorService doctorService;
    @Mock private RedisUtil redisUtil;
    @Mock private JwtTokenRevoker jwtTokenRevoker;

    @InjectMocks
    private AccountManager accountManager;

    private void setTokenPrefixes() {
        ReflectionTestUtils.setField(accountManager, "adminRefreshTokenPrefix", "refresh_token_");
    }

    /**
     * 空列表：返回空 PageInfo
     */
    @Test
    void listAccountDetailResponse_emptyList_returnsEmpty() {
        when(accountService.listAllAccountsByUserIdFilterAndRoleIdAndEnabledAndPage(null, null, null, 1, 10))
                .thenReturn(new PageInfo<>(List.of()));

        Result<PageResult<AccountDetailResponse>> result = accountManager
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
        when(accountService.listAllAccountsByUserIdFilterAndRoleIdAndEnabledAndPage(null, 1L, null, 1, 10))
                .thenReturn(pageInfo);
        when(adminService.listAdminsByIds(List.of(5001L))).thenReturn(List.of(admin));

        Result<PageResult<AccountDetailResponse>> result = accountManager
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
        assertEquals(RoleEnum.ADMIN.getName(), resp.getRole());
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
        when(accountService.listAllAccountsByUserIdFilterAndRoleIdAndEnabledAndPage(null, 2L, null, 1, 10))
                .thenReturn(pageInfo);
        when(doctorService.listDoctorsByIds(List.of(6001L))).thenReturn(List.of(doctor));

        Result<PageResult<AccountDetailResponse>> result = accountManager
                .listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(null, 2L, null, 1, 10);

        List<AccountDetailResponse> list = result.getData().getList();
        assertEquals(1, list.size());
        assertEquals("李医生", list.get(0).getUsername());
        assertEquals(RoleEnum.DOCTOR.getName(), list.get(0).getRole());
    }

    /**
     * 已废弃角色(roleId=3 护士)：不映射角色名称，role 为空
     */
    @Test
    void listAccountDetailResponse_deprecatedRole_returnsNullRoleName() {
        Account account = new Account();
        account.setId(3001L);
        account.setUserId(9001L);
        account.setRoleId(3L);
        account.setEnabled(true);

        when(accountService.listAllAccountsByUserIdFilterAndRoleIdAndEnabledAndPage(null, 3L, null, 1, 10))
                .thenReturn(new PageInfo<>(List.of(account)));

        Result<PageResult<AccountDetailResponse>> result = accountManager
                .listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(null, 3L, null, 1, 10);

        assertEquals(200, result.getCode());
        assertNull(result.getData().getList().get(0).getRole());
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
        when(accountService.listAllAccountsByUserIdFilterAndRoleIdAndEnabledAndPage(null, null, null, 1, 10))
                .thenReturn(pageInfo);
        when(adminService.listAdminsByIds(List.of(7001L))).thenReturn(List.of(admin));
        when(doctorService.listDoctorsByIds(List.of(8001L))).thenReturn(List.of(doctor));

        Result<PageResult<AccountDetailResponse>> result = accountManager
                .listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(null, null, null, 1, 10);

        List<AccountDetailResponse> list = result.getData().getList();
        assertEquals(2, list.size());
        assertEquals("管理员", list.get(0).getUsername());
        assertEquals("张医生", list.get(1).getUsername());
    }

    /**
     * 姓名无匹配：Manager 直接返回空页，不再查询 account
     */
    @Test
    void listAccountDetailResponse_usernameNoMatch_returnsEmptyWithoutAccountQuery() {
        when(adminService.listAdminsByLikeName("不存在")).thenReturn(List.of());
        when(doctorService.listDoctorsSimpleResponseByLikeDoctorNameAndDepartmentId("不存在", null))
                .thenReturn(List.of());
        when(userService.listUserIdsByNicknameLike("不存在")).thenReturn(List.of());

        Result<PageResult<AccountDetailResponse>> result = accountManager
                .listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(
                        "不存在", null, null, 1, 10);

        assertEquals(200, result.getCode());
        assertTrue(result.getData().getList().isEmpty());
        verifyNoInteractions(accountService);
    }

    /**
     * 姓名解析结果作为 userIds 过滤条件传入 AccountService
     */
    @Test
    void listAccountDetailResponse_usernameMatches_passesResolvedUserIds() {
        Admin admin = new Admin();
        admin.setId(11L);
        Doctor doctor = new Doctor();
        doctor.setId(22L);

        when(adminService.listAdminsByLikeName("张")).thenReturn(List.of(admin));
        when(doctorService.listDoctorsSimpleResponseByLikeDoctorNameAndDepartmentId("张", null))
                .thenReturn(List.of(doctor));
        when(userService.listUserIdsByNicknameLike("张")).thenReturn(List.of(33L));
        when(accountService.listAllAccountsByUserIdFilterAndRoleIdAndEnabledAndPage(
                List.of(11L, 22L, 33L), null, null, 1, 10))
                .thenReturn(new PageInfo<>(List.of()));

        Result<PageResult<AccountDetailResponse>> result = accountManager
                .listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(
                        "张", null, null, 1, 10);

        assertEquals(200, result.getCode());
        assertTrue(result.getData().getList().isEmpty());
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

        org.mockito.Mockito.verify(jwtTokenRevoker).revoke(1001L);
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

        org.mockito.Mockito.verify(jwtTokenRevoker).revoke(1001L);
        org.mockito.Mockito.verify(redisUtil).delete("refresh_token_1001");
    }

    /**
     * 分页元信息（pageNum/pageSize/total/totalPages）透传到 PageResult
     */
    @Test
    void accountPage_preservesSourcePaginationMetadata() {
        PageInfo<Account> source = new PageInfo<>();
        source.setPageNum(2);
        source.setPageSize(10);
        source.setTotal(35L);
        source.setPages(4);
        source.setList(List.of());

        when(accountService.listAllAccountsByUserIdFilterAndRoleIdAndEnabledAndPage(null, null, null, 2, 10))
                .thenReturn(source);

        Result<PageResult<AccountDetailResponse>> result = accountManager
                .listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(null, null, null, 2, 10);

        assertEquals(2, result.getData().getPageNum());
        assertEquals(10, result.getData().getPageSize());
        assertEquals(35L, result.getData().getTotal());
        assertEquals(4, result.getData().getTotalPages());
    }
}
