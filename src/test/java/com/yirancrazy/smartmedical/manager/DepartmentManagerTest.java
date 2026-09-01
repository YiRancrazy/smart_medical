package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Admin;
import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.request.admin.AdminDepartmentRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.AdminDepartmentSimpleResponse;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DepartmentManager 单测
 * 覆盖：insertDepartment 入参校验、listAllDepartmentsSimpleResponse 合并映射。
 * @Author: YiRanCrazy@gmail.com
 * @Description: 科室管理单测
 * @Datetime: 2026-07-24 16:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class DepartmentManagerTest {

    @Mock private DepartmentService departmentService;
    @Mock private AdminService adminService;
    @Mock private AccountService accountService;

    @InjectMocks
    private DepartmentManager departmentManager;

    // ===== insertDepartment 校验 =====

    /**
     * 请求为 null → Result.fail
     */
    @Test
    void insertDepartment_nullRequest_returnsFail() {
        Result<Long> result = departmentManager.insertDepartment(null);
        assertEquals(500, result.getCode());
        assertEquals("请求参数不能为空", result.getMessage());
    }

    /**
     * 科室名称为空 → Result.fail
     */
    @Test
    void insertDepartment_emptyName_returnsFail() {
        AdminDepartmentRequest req = buildRequest("", "1001", 1L);
        Result<Long> result = departmentManager.insertDepartment(req);
        assertEquals(500, result.getCode());
        assertEquals("科室名称不能为空", result.getMessage());
    }

    /**
     * 科室编号为空 → Result.fail
     */
    @Test
    void insertDepartment_nullSn_returnsFail() {
        AdminDepartmentRequest req = buildRequest("内科", null, 1L);
        Result<Long> result = departmentManager.insertDepartment(req);
        assertEquals(500, result.getCode());
        assertEquals("科室编号不能为空", result.getMessage());
    }

    /**
     * 负责人ID为空 → Result.fail
     */
    @Test
    void insertDepartment_nullManagerId_returnsFail() {
        AdminDepartmentRequest req = buildRequest("内科", "1001", null);
        Result<Long> result = departmentManager.insertDepartment(req);
        assertEquals(500, result.getCode());
        assertEquals("科室负责人不能为空", result.getMessage());
    }

    /**
     * happy path：参数齐全 → 调 service.insertDepartment 并返回 ID
     */
    @Test
    void insertDepartment_happyPath_returnsId() {
        AdminDepartmentRequest req = buildRequest("内科", "1001", 5001L);
        req.setType("1");
        req.setPhone("13800000000");
        req.setStatus("1");

        Result<Long> result = departmentManager.insertDepartment(req);

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        verify(departmentService).insertDepartment(any(Department.class));
    }

    // ===== listAllDepartmentsSimpleResponse 合并映射 =====

    /**
     * happy path：科室+管理员+账户正确合并到 VO
     */
    @Test
    void listAllDepartmentsSimpleResponse_happyPath_mapsCorrectly() {
        Department dept = new Department();
        dept.setId(1001L);
        dept.setSn(1001L);
        dept.setName("内科");
        dept.setManager(5001L);
        dept.setParentDepartmentId(0L);
        dept.setStatus(1);
        dept.setType("1");

        Admin admin = new Admin();
        admin.setId(5001L);
        admin.setName("张管理员");

        Account account = new Account();
        account.setUserId(5001L);
        account.setRoleId(1L);
        account.setPhone("13900000000");

        when(departmentService.listAllDepartment()).thenReturn(List.of(dept));
        when(adminService.listAdminsByIds(List.of(5001L))).thenReturn(List.of(admin));
        when(accountService.listAccountsByUserIds(List.of(5001L))).thenReturn(List.of(account));

        Result<List<AdminDepartmentSimpleResponse>> result = departmentManager.listAllDepartmentsSimpleResponse();

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        AdminDepartmentSimpleResponse vo = result.getData().get(0);
        assertEquals("1001", vo.getId());
        assertEquals("内科", vo.getName());
        assertEquals("5001", vo.getManagerId());
        assertEquals("张管理员", vo.getManagerName());
        assertEquals("13900000000", vo.getManagerPhone());
    }

    /**
     * 管理员不存在 → 该科室被跳过（log.warn + continue）
     */
    @Test
    void listAllDepartmentsSimpleResponse_missingAdmin_skipped() {
        Department dept = new Department();
        dept.setId(1002L);
        dept.setManager(5002L);

        when(departmentService.listAllDepartment()).thenReturn(List.of(dept));
        when(adminService.listAdminsByIds(List.of(5002L))).thenReturn(List.of());
        when(accountService.listAccountsByUserIds(List.of(5002L))).thenReturn(List.of());

        Result<List<AdminDepartmentSimpleResponse>> result = departmentManager.listAllDepartmentsSimpleResponse();

        assertEquals(200, result.getCode());
        assertTrue(result.getData().isEmpty());
    }

    // ===== 辅助构造 =====

    private AdminDepartmentRequest buildRequest(String name, String sn, Long managerId) {
        AdminDepartmentRequest req = new AdminDepartmentRequest();
        req.setName(name);
        req.setSn(sn);
        req.setManagerId(managerId);
        return req;
    }
}
