package com.yirancrazy.smartmedical.controller.admin;

import com.yirancrazy.smartmedical.manager.DepartmentManager;
import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.request.admin.AdminDepartmentRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.AdminDepartmentSimpleResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-06 20:07
 * @Version: 1.0
 */

@RestController
@RequestMapping("/api/admin/v1/department")
@Tag(name = "管理员-科室管理")
@RequiredArgsConstructor
public class AdminDepartmentControllerV1 {

    private final DepartmentManager departmentManager;

    /**
     * 添加科室
     * @param adminDepartmentRequest 科室信息请求参数
     * @return 添加结果
     */
    @PostMapping("/")
    @Operation(summary = "添加科室", description = "添加新科室")
    public Result<Long> insertDepartment(@Valid @RequestBody AdminDepartmentRequest adminDepartmentRequest) {
        return departmentManager.insertDepartment(adminDepartmentRequest);
    }


    /**
     * 获取所有科室列表
     * @return 科室列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有科室列表")
    public Result<List<AdminDepartmentSimpleResponse>> listAllDepartmentsSimpleResponse() {
        return departmentManager.listAllDepartmentsSimpleResponse();
    }

    /**
     * 分页获取所有科室列表
     * @param current 当前页
     * @param size 每页数量
     * @return 科室列表
     */
    @GetMapping("/list/simple/{current}/{size}")
    @Operation(summary = "分页获取所有管理员简单响应")
    public Result<PageResult<AdminDepartmentSimpleResponse>> listAdminAdminSimpleResponseByPage(@PathVariable  Integer current, @PathVariable  Integer size){
        return departmentManager.listAllDepartmentsSimpleResponseByPageAndSnAsc(current,size);
    }

    /**
     * 获取所有父级科室列表
     * @param current 当前页
     * @param size 每页数量
     * @return 父级科室列表
     */
    @GetMapping("/list/parent/{current}/{size}")
    @Operation(summary = "获取所有父级科室列表")
    public Result<PageResult<AdminDepartmentSimpleResponse>> listAllParentDepartmentSimpleResponse(@PathVariable Integer current, @PathVariable Integer size) {
        return departmentManager.listAllParentDepartmentsSimpleResponse( current, size );
    }

    /**
     * 获取所有子级科室列表
     * @param current 当前页
     * @param size 每页数量
     * @return 子级科室列表
     */
    @GetMapping("/list/child/{current}/{size}")
    @Operation(summary = "获取所有子级科室列表")
    public Result<PageResult<AdminDepartmentSimpleResponse>> listAllChildDepartmentSimpleResponse( @PathVariable Integer current, @PathVariable Integer size) {
        return departmentManager.listAllChildDepartmentsSimpleResponse( current, size );
    }


    /**
     * 多条件检索科室列表
     * @param name 科室名称
     * @param sn 科室编号
     * @param type 科室类型
     * @param status 科室状态
     * @param parentId 父级科室ID
     * @param current 当前页
     * @param size 每页数量
     * @return 科室列表
     */
    @GetMapping("/list/conditions")
    @Operation(summary = "多条件检索科室列表")
    public Result<PageResult<AdminDepartmentSimpleResponse>> listAllDepartmentsSimpleResponseByNameAndSnAndTypeAndStatusAndParentId(
            @RequestParam(required = false, defaultValue = "") String name,
            @RequestParam(required = false, defaultValue = "") String sn,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return departmentManager.listAllDepartmentsSimpleResponseByNameAndSnAndTypeAndStatusAndParentId(name, sn, type, status, parentId,current, size);
    }

    /**
     * 获取所有科室列表树形结构
     * @return 科室列表树形结构
     */
    @GetMapping("/list/tree")
    @Operation(summary = "获取所有科室列表树形结构")
    public Result<Map<AdminDepartmentSimpleResponse, List<AdminDepartmentSimpleResponse>>> listAllDepartmentsSimpleResponseByTree() {
        return departmentManager.listAllDepartmentsSimpleResponseByPageAndSnAscAndTree();
    }

    /**
     * 根据ID获取科室
     * @param id 科室ID
     * @return 科室信息
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "管理员端 - 科室详情")
    @Parameter(name = "id", description = "科室ID", required = true)
    public Result<Department> getDepartmentById(@PathVariable Long id) {
        return departmentManager.getDepartmentById(id);
    }

    /**
     * 更新科室
     * @param id 科室ID
     * @param adminDepartmentRequest 科室信息请求参数
     * @return 更新结果
     */
    @PutMapping("/{id:\\d+}")
    @Operation(summary = "管理员端 - 更新科室")
    @Parameter(name = "id", description = "科室ID", required = true)
    public Result<Integer> updateDepartment(@PathVariable Long id, @Valid @RequestBody AdminDepartmentRequest adminDepartmentRequest) {
        return departmentManager.updateDepartment(id, adminDepartmentRequest);
    }

    /**
     * 删除科室
     * @param id 科室ID
     * @return 删除结果
     */
    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "管理员端 - 删除科室")
    @Parameter(name = "id", description = "科室ID", required = true)
    public Result<Integer> deleteDepartmentById(@PathVariable Long id) {
        return departmentManager.deleteDepartmentById(id);
    }
}