package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.IdUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Admin;
import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.request.admin.AdminDepartmentRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminDoctorSimpleResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminRegistrationScheduleTemplateDetail;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.AdminDepartmentSimpleResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import com.yirancrazy.smartmedical.pojo.vo.ChildDepartmentBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.ParentDepartmentBaseInfo;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.AdminService;
import com.yirancrazy.smartmedical.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 科室Manager
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
@Slf4j
public class DepartmentManager {

    private final DepartmentService departmentService;
    private final AdminService adminService;
    private final AccountService accountService;

    /**
     * 添加科室
     * @param item 科室信息
     * @return 添加结果
     */
    public Result<Long> insertDepartment(AdminDepartmentRequest item) {
        if (item == null) {
            return Result.fail("请求参数不能为空");
        }
        if (item.getName() == null || item.getName().isBlank()) {
            return Result.fail("科室名称不能为空");
        }
        Long sn = Convert.toLong(item.getSn(), null);
        if (sn == null) {
            return Result.fail("科室编号不能为空");
        }
        Long managerId = item.getManagerId();
        if (managerId == null) {
            return Result.fail("科室负责人不能为空");
        }
        Long parentDepartmentId = item.getParentDepartmentId() == null ? 0L : item.getParentDepartmentId();

        Department department = new Department();
        department.setId(IdUtil.getSnowflakeNextId());
        department.setSn(sn);
        department.setName(item.getName());
        department.setType(item.getType());
        department.setParentDepartmentId(parentDepartmentId);
        department.setManager(managerId);
        department.setPhone(item.getPhone());
        department.setAddress(item.getAddress());
        department.setStatus(Convert.toInt(item.getStatus(), 1));
        department.setDescription(item.getDescription());

        departmentService.insertDepartment(department);
        return Result.success(department.getId());
    }

    /**
     * 根据ID获取科室信息
     * @param id 科室ID
     * @return 科室信息
     */
    public Result<Department> getDepartmentById(Long id) {
        Department department = departmentService.getDepartmentById(id);
        return Result.success(department);
    }

    /**
     * 更新科室信息
     * @param id 科室ID
     * @param item 科室信息请求参数
     * @return 更新结果
     */
    public Result<Integer> updateDepartment(Long id, AdminDepartmentRequest item) {
        if (id == null) {
            return Result.fail("科室ID不能为空");
        }
        Department department = new Department();
        department.setId(id);
        department.setSn(Convert.toLong(item.getSn(), null));
        department.setName(item.getName());
        department.setType(item.getType());
        department.setParentDepartmentId(item.getParentDepartmentId() == null ? 0L : item.getParentDepartmentId());
        department.setManager(item.getManagerId());
        department.setPhone(item.getPhone());
        department.setAddress(item.getAddress());
        department.setStatus(Convert.toInt(item.getStatus(), 1));
        department.setDescription(item.getDescription());

        int result = departmentService.updateDepartmentById(department);
        return Result.success(result);
    }

    /**
     * 根据ID删除科室
     * @param id 科室ID
     * @return 删除结果
     */
    public Result<Integer> deleteDepartmentById(Long id) {
        int result = departmentService.deleteDepartmentById(id);
        return Result.success(result);
    }

    /**
     * 获取所有科室列表
     * @return 科室列表
     */
    public Result<List<Department>> getAllDepartments() {
        List<Department> departments = departmentService.listAllDepartment();
        return Result.success(departments);
    }

    /**
     * 分页查询科室
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    public Result<PageInfo<Department>> getDepartmentPage(Integer pageNum, Integer pageSize) {
        PageInfo<Department> pageInfo = departmentService.listDepartmentsByPage(pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 批量删除科室
     * @param ids 科室ID列表
     * @return 删除结果
     */
    public Result<Integer> deleteBatch(List<Long> ids) {
        int result = departmentService.deleteBatch(ids);
        return Result.success(result);
    }

    /**
     * 获取所有一级科室列表
     * @return 一级科室列表
     */
    public Result<List<Department>> getAllParentDepartments() {
        List<Department> departments = departmentService.listAllParentDepartments();
        return Result.success(departments);
    }

    /**
     * 获取所有二级科室列表
     * @return 二级科室列表
     */
    public Result<List<Department>> getAllNonParentDepartments() {
        List<Department> departments = departmentService.listAllNonParentDepartments();
        return Result.success(departments);
    }

    /**
     * 根据一级科室ID获取所有二级科室列表
     * @param parentId 一级科室ID
     * @return 二级科室列表
     */
    public Result<List<Department>> getAllDepartmentsByParentDepartmentId(Long parentId) {
        List<Department> departments = departmentService.listAllDepartmentsByParentDepartmentId(parentId);
        return Result.success(departments);
    }

    /**
     * 获取所有一级科室基本信息列表
     * @return 一级科室基本信息列表
     */
    public Result<List<ParentDepartmentBaseInfo>> listAllParentDepartmentBaseInfo() {
        List<Department> departments = departmentService.listAllParentDepartments();
        List<ParentDepartmentBaseInfo> parentDepartmentBaseInfos = departments
                .stream()
                .map(department -> new ParentDepartmentBaseInfo(String.valueOf(department
                        .getId()), department.getName())).toList();
        return Result.success(parentDepartmentBaseInfos);
    }


    /**
     * 获取所有二级科室基本信息列表
     * @return 二级科室基本信息列表
     */
    public Result<List<ChildDepartmentBaseInfo>> listAllNonParentDepartmentBaseInfo() {
        List<Department> departments = departmentService.listAllNonParentDepartments();
        List<ChildDepartmentBaseInfo> childDepartmentBaseInfos = departments
                .stream()
                .map(department -> new ChildDepartmentBaseInfo(String.valueOf(department
                        .getId()), department.getName(), String.valueOf(department.getParentDepartmentId()), department.getDescription())).toList();
        return Result.success(childDepartmentBaseInfos);
    }

    /**
     * 根据一级科室ID获取所有二级科室基本信息列表
     * @param parentId 一级科室ID
     * @return 二级科室基本信息列表
     */
    public Result<List<ChildDepartmentBaseInfo>> listAllDepartmentBaseInfoByParentDepartmentId(Long parentId) {
        List<Department> departments = departmentService.listAllDepartmentsByParentDepartmentId(parentId);
        List<ChildDepartmentBaseInfo> childDepartmentBaseInfos = departments
                .stream()
                .map(department -> new ChildDepartmentBaseInfo(String.valueOf(department
                        .getId()), department.getName(), String.valueOf(department.getParentDepartmentId()), department.getDescription())).toList();
        return Result.success(childDepartmentBaseInfos);
    }

    /**
     * 获取所有科室基本信息列表
     * @return 科室基本信息列表
     */
    // todo 写完了没测试
    public Result<List<AdminDepartmentSimpleResponse>> listAllDepartmentsSimpleResponse() {
        List<AdminDepartmentSimpleResponse> result = new ArrayList<>();
        List<Department> departments = departmentService.listAllDepartment();
        List<Long> managerIds = departments
                .stream()
                .map(Department::getManager)
                .toList();  // 获取所有负责人的id
        List<Admin> admins = adminService.listAdminsByIds(managerIds);
        List<Account> accounts = accountService.listAccountsByUserIds(managerIds);

        result = mergeDepartmentAndAdminAndAccount(departments, admins, accounts);

        return Result.success(result);

    }

    /**
     * 获取所有科室基本信息列表-树形
     * @return 科室基本信息列表
     */
    public Result<Map<AdminDepartmentSimpleResponse, List<AdminDepartmentSimpleResponse>>> listAllDepartmentsSimpleResponseByPageAndSnAscAndTree() {
        List<Department> departments = departmentService.listAllDepartment();  // 获取所有科室信息
        List<Long> managerIds = departments
                .stream()
                .map(Department::getManager)
                .toList();  // 获取所有负责人的id
        List<Admin> admins = adminService.listAdminsByIds(managerIds); // 获取所有负责人的信息
        List<Account> accounts = accountService.listAccountsByUserIds(managerIds);  // 获取所有负责人的账户信息

        List<AdminDepartmentSimpleResponse> list = mergeDepartmentAndAdminAndAccount(departments, admins, accounts); // 合并科室信息、负责人信息、账户信息


        // 查找出所有一级科室
        List<AdminDepartmentSimpleResponse> parentDepartmentList = list
                .stream()
                .filter( item-> "null".equals(item.getParentDepartmentId()))
                .toList();

        Map<AdminDepartmentSimpleResponse,List<AdminDepartmentSimpleResponse>> result = new HashMap<>();
        for (AdminDepartmentSimpleResponse item: parentDepartmentList) {
            List<AdminDepartmentSimpleResponse> childDepartmentList = list
                    .stream()
                    .filter( item1-> item.getId().equals(item1.getParentDepartmentId()) )
                    .toList();
            result.put(item, childDepartmentList);
        }

        return Result.success(result);
    }


    /**
     * 分页获取所有科室基本信息列表
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 科室基本信息列表
     */
    public Result<PageResult<AdminDepartmentSimpleResponse>> listAllDepartmentsSimpleResponseByPageAndSnAsc(int pageNum, int pageSize) {
        List<AdminDepartmentSimpleResponse> result = new ArrayList<>();
        PageHelper.startPage(pageNum, pageSize);
        List<Department> departments = departmentService.listAllDepartment();

        PageInfo<Department> pageinfo = new PageInfo<>( departments);
        List<Long> managerIds = departments
                .stream()
                .map(Department::getManager)
                .toList();  // 获取所有负责人的id
        List<Admin> admins = adminService.listAdminsByIds(managerIds);
        List<Account> accounts = accountService.listAccountsByUserIds(managerIds);

        result = mergeDepartmentAndAdminAndAccount(departments, admins, accounts);

        return Result.success(new PageResult<>(pageinfo,result));
    }

    /**
     * 分页获取所有一级科室基本信息列表
     * @return 一级科室基本信息列表
     */
    public Result<PageResult<AdminDepartmentSimpleResponse>> listAllParentDepartmentsSimpleResponse(int pageNum, int pageSize) {
        List<AdminDepartmentSimpleResponse> result = new ArrayList<>();
        PageHelper.startPage(pageNum, pageSize);
        List<Department> departments = departmentService.listAllParentDepartments();
        PageInfo<Department> pageinfo = new PageInfo<>( departments);
        List<Long> managerIds = departments
                .stream()
                .map(Department::getManager)
                .toList();
        List<Admin> admins = adminService.listAdminsByIds(managerIds);
        List<Account> accounts = accountService.listAccountsByUserIds(managerIds);
        result = mergeDepartmentAndAdminAndAccount(departments, admins, accounts);
        return Result.success(new PageResult<>(pageinfo,result));
    }

    /**
     * 获取所有二级科室基本信息列表
     * @return 二级科室基本信息列表
     */
    public Result<PageResult<AdminDepartmentSimpleResponse>> listAllChildDepartmentsSimpleResponse(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum,pageSize);        // 分页
        List<AdminDepartmentSimpleResponse> result = new ArrayList<>();
        List<Department> departments = departmentService.listAllNonParentDepartments();
        PageInfo<Department> pageinfo = new PageInfo<>(departments);
        List<Long> managerIds = departments
                .stream()
                .map(Department::getManager)
                .toList();
        List<Admin> admins = adminService.listAdminsByIds(managerIds);
        List<Account> accounts = accountService.listAccountsByUserIds(managerIds);
        result = mergeDepartmentAndAdminAndAccount(departments, admins, accounts);
        return Result.success(new PageResult<>(pageinfo,result));
    }


    /**
     * 合并科室、管理员、账户信息
     * @param departments 科室列表
     * @param admins 管理员列表
     * @param accounts 账户列表
     * @return 合并后的结果
     */
    private List<AdminDepartmentSimpleResponse>  mergeDepartmentAndAdminAndAccount(List<Department> departments, List<Admin> admins, List<Account> accounts) {
        List<AdminDepartmentSimpleResponse> result = new ArrayList<>();

        for(Department department : departments){
            AdminDepartmentSimpleResponse item = new AdminDepartmentSimpleResponse();
            Admin admin = admins.stream()
                    .filter(a -> a.getId().equals(department.getManager()))
                    .findFirst().orElse(null);
            Account account = accounts.stream()
                    .filter(a -> a.getUserId().equals(department.getManager()))
                    .filter(a->a.getRoleId().equals(1L))
                    .findFirst().orElse(null);
            if (admin == null || account == null) {
                log.warn("跳过科室 {}：未找到对应的管理员 admin={}, account={}", department.getId(), admin, account);
                continue;
            }
            item.setId(String.valueOf(department.getId()));
            item.setSn(String.valueOf(department.getSn()));
            item.setName(department.getName());
            item.setType(department.getType());
            item.setDescription(department.getDescription());
            item.setManagerId(String.valueOf(department.getManager()));
            item.setManagerName(admin.getName());
            item.setManagerPhone(account.getPhone());
            item.setPhone(department.getPhone());
            item.setParentDepartmentId(String.valueOf(department.getParentDepartmentId()));
            String parentId = item.getParentDepartmentId();
            String parentName = null;
            if (parentId != null && !"null".equals(parentId) && !"0".equals(parentId)) {
                parentName = departments.stream()
                        .filter(d -> parentId.equals(String.valueOf(d.getId())))
                        .map(Department::getName)
                        .findFirst().orElse(null);
            }
            item.setParentDepartmentName(parentName);
            item.setAddress(department.getAddress());
            item.setStatus(String.valueOf(department.getStatus()));
            result.add(item);

        }

        return result;
    }

    /**
     * 根据名称、编号、类型、状态、上级科室ID分页查询科室列表
     * @param name 科室名称 (可为空)
     * @param sn 科室编号 (可为空)
     * @param type 科室类型 (可为空)
     * @param status 科室状态 (可为空)
     * @param parentId 上级科室ID (可为空)
     * @return 科室列表
     */
    public Result<PageResult<AdminDepartmentSimpleResponse>> listAllDepartmentsSimpleResponseByNameAndSnAndTypeAndStatusAndParentId(String name, String sn, Integer type, Integer status, Long parentId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        List<Department> departments = departmentService.listDepartmentByParentIdAndStatusAndTypeAndLikeNameAndLikeSn(
                parentId,
                status,
                type,
                name,
                Convert.toLong(sn, null)
        );

        // 封装成 PageInfo 对象
        PageInfo<Department> pageinfo = new PageInfo<>(departments);
        if (departments.isEmpty()) {
            return Result.success(new PageResult<>(pageinfo, new ArrayList<>()));
        }
        List<Long> managerIds = departments
                .stream()
                .map(Department::getManager)
                .toList();
                List<Admin> admins = adminService.listAdminsByIds(managerIds);
                List<Account> accounts = accountService.listAccountsByUserIds(managerIds);
        List<AdminDepartmentSimpleResponse> result = mergeDepartmentAndAdminAndAccount(departments, admins, accounts);
        return Result.success(new PageResult<>(pageinfo,result));
    }
}
