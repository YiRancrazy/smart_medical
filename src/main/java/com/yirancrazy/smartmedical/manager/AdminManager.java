package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Admin;
import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.AdminAdminSimpleResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.AdminService;
import com.yirancrazy.smartmedical.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
@Slf4j
public class AdminManager {

    private final AdminService adminService;
    private final AccountService accountService;
    private final DepartmentService departmentService;

    public int addAdmin(Admin admin) {
        admin.setId(IdUtil.getSnowflakeNextId());
        return adminService.insertAdmin(admin);
    }

    public Admin getAdminById(Long id) {
        return adminService.getAdminById(id);
    }

    /**
     * 根据管理员id获取管理员的简单响应
     * @param adminId 管理员id
     * @return 管理员简单响应
     */
    public Result<AdminAdminSimpleResponse> getAdminAdminSimpleResponseByAdminId(Long adminId){
        Admin admin = adminService.getAdminById(adminId);
        if (admin == null) {
            return Result.fail("管理员不存在");
        }
        Account account = accountService.getAccountByUserId(adminId);
        if (account == null) {
            return Result.fail("管理员账号不存在");
        }
        Department department = departmentService.getDepartmentById(admin.getDepartmentId());

        AdminAdminSimpleResponse result = new AdminAdminSimpleResponse();
        result.setId(String.valueOf(admin.getId()));
        result.setUsername(admin.getName());
        result.setPhone(account.getPhone());
        result.setAvatar(admin.getAvatar());
        result.setRemark(admin.getRemark());
        result.setDepartmentId(String.valueOf(admin.getDepartmentId()));
        result.setDepartmentName(department == null ? "" : department.getName());
        result.setRole(account.getRoleId().toString());
        result.setRoleId(account.getRoleId().toString());
        result.setStatus(account.getEnabled().toString());
        result.setEmail(account.getEmail());
        return Result.success(result);
    }

    /**
     * 获取所有管理员简单响应
     * @return 管理员简单响应列表
     */
    public Result<List<AdminAdminSimpleResponse>> listAdminAdminSimpleResponse(){
        List<AdminAdminSimpleResponse> result = new ArrayList<>();
        List<Admin> admins = adminService.listAdmins();
        List<Account> accounts = accountService.listAccountsByUserIds(
                admins.stream().map(Admin::getId).toList()
        );
        List<Department> departments = departmentService.listAllDepartment();

        for(Admin admin : admins){
            AdminAdminSimpleResponse item = new AdminAdminSimpleResponse();
            Account account = accounts.stream().filter(account1 -> account1.getUserId().equals(admin.getId())).findFirst().orElse(null);
            Department department = departments.stream().filter(department1 -> department1.getId().equals(admin.getDepartmentId())).findFirst().orElse(null);
            if (account == null || department == null) {
                log.warn("跳过管理员 {}：未找到对应的 account={} 或 department={}", admin.getId(), account, department);
                continue;
            }
            item.setId(String.valueOf(admin.getId()));
            item.setUsername(admin.getName());
            item.setPhone(account.getPhone());
            item.setAvatar(admin.getAvatar());
            item.setRemark(admin.getRemark());
            item.setDepartmentId(String.valueOf(admin.getDepartmentId()));
            item.setDepartmentName(department.getName());
            item.setRole(account.getRoleId().toString());
            item.setRoleId(account.getRoleId().toString());
            item.setStatus(account.getEnabled().toString());
            item.setEmail(account.getEmail());
            result.add(item);
        }

        return Result.success(result);

    }

    /**
     * 分页获取所有管理员简单响应
     * @param current 当前页
     * @param size 页大小
     * @return 管理员简单响应列表
     */
    public Result<PageResult<AdminAdminSimpleResponse>> listAdminAdminSimpleResponseByPage(Integer current, Integer size){
        List<AdminAdminSimpleResponse> result = new ArrayList<>();
        PageInfo<Admin> adminsByPage = adminService.listAdminsByPage(current, size);
        List<Admin> admins = adminsByPage.getList();

        // 按科室id排序
        admins.sort(Comparator.comparing(Admin::getDepartmentId));

        List<Account> accounts = accountService.listAccountsByUserIds(
                admins.stream().map(Admin::getId).toList()
        );
        List<Department> departments = departmentService.listAllDepartment();

        for(Admin admin : admins){
            AdminAdminSimpleResponse item = new AdminAdminSimpleResponse();
            Account account = accounts.stream().filter(account1 -> account1.getUserId().equals(admin.getId())).findFirst().orElse(null);
            Department department = departments.stream().filter(department1 -> department1.getId().equals(admin.getDepartmentId())).findFirst().orElse(null);
            if (account == null || department == null) {
                log.warn("跳过管理员 {}：未找到对应的 account={} 或 department={}", admin.getId(), account, department);
                continue;
            }
            item.setId(String.valueOf(admin.getId()));
            item.setUsername(admin.getName());
            item.setPhone(account.getPhone());
            item.setAvatar(admin.getAvatar());
            item.setRemark(admin.getRemark());
            item.setDepartmentId(String.valueOf(admin.getDepartmentId()));
            item.setDepartmentName(department.getName());
            item.setRole(account.getRoleId().toString());
            item.setRoleId(account.getRoleId().toString());
            item.setStatus(account.getEnabled().toString());
            item.setEmail(account.getEmail());
            result.add(item);
        }

        PageResult<AdminAdminSimpleResponse> result1 = new PageResult<>(adminsByPage,result);
        return Result.success(result1);
    }
}
