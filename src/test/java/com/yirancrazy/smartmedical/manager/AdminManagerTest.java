package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Admin;
import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.AdminAdminSimpleResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.AdminService;
import com.yirancrazy.smartmedical.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AdminManager 单测
 */
@ExtendWith(MockitoExtension.class)
class AdminManagerTest {

    @Mock private AdminService adminService;
    @Mock private AccountService accountService;
    @Mock private DepartmentService departmentService;

    @InjectMocks
    private AdminManager adminManager;

    @Test
    void addAdmin_assignsSnowflakeId() {
        when(adminService.insertAdmin(any(Admin.class))).thenReturn(1);
        Admin a = new Admin();

        int rows = adminManager.addAdmin(a);

        assertEquals(1, rows);
        assertNotNull(a.getId());
        verify(adminService).insertAdmin(a);
    }

    @Test
    void getAdminById_delegates() {
        Admin a = new Admin();
        a.setId(1L);
        when(adminService.getAdminById(1L)).thenReturn(a);

        Admin result = adminManager.getAdminById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getAdminAdminSimpleResponseByAdminId_assembles() {
        Admin admin = new Admin();
        admin.setId(7L);
        admin.setName("Alice");
        admin.setDepartmentId(11L);
        admin.setAvatar("a.jpg");
        admin.setRemark("r");

        Account account = new Account();
        account.setUserId(7L);
        account.setPhone("138");
        account.setRoleId(1L);
        account.setEnabled(true);
        account.setEmail("a@b.com");

        Department department = new Department();
        department.setId(11L);
        department.setName("内科");

        when(adminService.getAdminById(7L)).thenReturn(admin);
        when(accountService.getAccountByUserId(7L)).thenReturn(account);
        when(departmentService.getDepartmentById(11L)).thenReturn(department);

        Result<AdminAdminSimpleResponse> result = adminManager.getAdminAdminSimpleResponseByAdminId(7L);

        assertEquals(200, result.getCode());
        AdminAdminSimpleResponse dto = result.getData();
        assertEquals("7", dto.getId());
        assertEquals("Alice", dto.getUsername());
        assertEquals("138", dto.getPhone());
        assertEquals("内科", dto.getDepartmentName());
    }

    @Test
    void listAdminAdminSimpleResponse_skipsMissingAccountOrDepartment() {
        Admin admin = new Admin();
        admin.setId(7L);
        admin.setName("A");
        admin.setDepartmentId(11L);
        when(adminService.listAdmins()).thenReturn(List.of(admin));
        when(accountService.listAccountsByUserIds(any())).thenReturn(List.of()); // empty — no account
        when(departmentService.listAllDepartment()).thenReturn(List.of());

        Result<List<AdminAdminSimpleResponse>> result = adminManager.listAdminAdminSimpleResponse();

        assertEquals(200, result.getCode());
        assertEquals(0, result.getData().size(), "admin with missing account or dept is skipped");
    }

    @Test
    void listAdminAdminSimpleResponseByPage_assemblesFromPage() {
        Admin admin = new Admin();
        admin.setId(7L);
        admin.setName("A");
        admin.setDepartmentId(11L);

        Account account = new Account();
        account.setUserId(7L);
        account.setPhone("138");
        account.setRoleId(1L);
        account.setEnabled(true);
        account.setEmail("a@b.com");

        Department department = new Department();
        department.setId(11L);
        department.setName("内科");

        com.github.pagehelper.PageInfo<Admin> pageInfo = new com.github.pagehelper.PageInfo<>();
        pageInfo.setPageNum(1);
        pageInfo.setPageSize(10);
        pageInfo.setTotal(1L);
        pageInfo.setPages(1);
        pageInfo.setList(new java.util.ArrayList<>(List.of(admin)));

        when(adminService.listAdminsByPage(1, 10)).thenReturn(pageInfo);
        when(accountService.listAccountsByUserIds(any())).thenReturn(List.of(account));
        when(departmentService.listAllDepartment()).thenReturn(List.of(department));

        Result<PageResult<AdminAdminSimpleResponse>> result = adminManager.listAdminAdminSimpleResponseByPage(1, 10);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().getList().size());
        assertEquals(1L, result.getData().getTotal());
    }
}
