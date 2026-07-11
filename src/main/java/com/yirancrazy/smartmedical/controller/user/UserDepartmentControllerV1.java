package com.yirancrazy.smartmedical.controller.user;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.manager.DepartmentManager;
import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.request.admin.AdminDepartmentRequest;
import com.yirancrazy.smartmedical.pojo.vo.ChildDepartmentBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.ParentDepartmentBaseInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 科室控制器
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/v1/department")
@Tag(name = "科室管理", description = "科室管理接口")
public class UserDepartmentControllerV1 {

    private final DepartmentManager departmentManager;


    /**
     * 根据ID获取科室信息
     * @param id 科室ID
     * @return 科室信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取科室", description = "根据科室ID获取科室信息")
    @Parameter(name = "id", description = "科室ID", required = true)
    public Result<Department> getDepartmentById(@PathVariable String id) {
        return departmentManager.getDepartmentById(Long.parseLong(id));
    }

    /**
     * 更新科室信息
     * @param department 科室信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @Operation(summary = "更新科室", description = "更新科室信息")
    public Result<Integer> updateDepartment(@RequestBody Department department) {
        return departmentManager.updateDepartment(department);
    }

    /**
     * 根据ID删除科室
     * @param id 科室ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除科室", description = "根据科室ID删除科室")
    @Parameter(name = "id", description = "科室ID", required = true)
    public Result<Integer> deleteDepartmentById(@PathVariable Long id) {
        return departmentManager.deleteDepartmentById(id);
    }

    /**
     * 获取所有科室列表
     * @return 科室列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有科室", description = "获取所有科室列表")
    public Result<List<Department>> getAllDepartments() {
        return departmentManager.getAllDepartments();
    }

    /**
     * 分页查询科室
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询科室", description = "分页查询科室列表")
    @Parameter(name = "pageNum", description = "页码", required = true)
    @Parameter(name = "pageSize", description = "页面大小", required = true)
    public Result<PageInfo<Department>> getDepartmentPage(
            @RequestParam Integer pageNum,
            @RequestParam Integer pageSize) {
        return departmentManager.getDepartmentPage(pageNum, pageSize);
    }

    /**
     * 批量删除科室
     * @param ids 科室ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除科室", description = "批量删除科室")
    public Result<Integer> deleteBatch(@RequestBody List<Long> ids) {
        return departmentManager.deleteBatch(ids);
    }

    /**
     * 获取所有一级科室列表
     * @return 一级科室列表
     */
    @GetMapping("/parent/list")
    @Operation(summary = "获取所有一级科室", description = "获取所有一级科室列表")
    public Result<List<Department>> getAllParentDepartments() {
        return departmentManager.getAllParentDepartments();
    }

    /**
     * 获取所有二级科室列表
     * @return 二级科室列表
     */
    @GetMapping("/child/list")
    @Operation(summary = "获取所有二级科室", description = "获取所有二级科室列表")
    public Result<List<Department>> getAllNonParentDepartments() {
        return departmentManager.getAllNonParentDepartments();
    }

    /**
     * 根据一级科室ID获取所有二级科室列表
     * @param parentId 一级科室ID
     * @return 二级科室列表
     */
    @GetMapping("/child/list/parentId")
    @Operation(summary = "根据一级科室获取二级科室", description = "根据一级科室ID获取所有二级科室列表")
    @Parameter(name = "parentId", description = "一级科室ID", required = true)
    public Result<List<Department>> getAllDepartmentsByParentDepartmentId(@RequestParam("parentId") Long parentId) {
        return departmentManager.getAllDepartmentsByParentDepartmentId(parentId);
    }

    /**
     * 获取所有一级科室基本信息列表
     * @return 一级科室基本信息列表
     */
    @GetMapping("/parent/baseInfo/list")
    @Operation(summary = "获取所有一级科室基本信息", description = "获取所有一级科室基本信息列表")
    public Result<List<ParentDepartmentBaseInfo>> listAllParentDepartmentBaseInfo() {
        return departmentManager.listAllParentDepartmentBaseInfo();
    }

    /**
     * 获取所有二级科室基本信息列表
     * @return 二级科室基本信息列表
     */
    @GetMapping("/child/baseInfo/list")
    @Operation(summary = "获取所有二级科室基本信息", description = "获取所有二级科室基本信息列表")
    public Result<List<ChildDepartmentBaseInfo>> listAllNonParentDepartmentBaseInfo() {
        return departmentManager.listAllNonParentDepartmentBaseInfo();
    }

    /**
     * 根据一级科室ID获取所有二级科室基本信息列表
     * @param parentId 一级科室ID
     * @return 二级科室基本信息列表
     */
    @GetMapping("/child/baseInfo/list/parentId")
    @Operation(summary = "根据一级科室获取二级科室基本信息", description = "根据一级科室ID获取所有二级科室基本信息列表")
    @Parameter(name = "parentId", description = "一级科室ID", required = true)
    public Result<List<ChildDepartmentBaseInfo>> listAllDepartmentBaseInfoByParentDepartmentId(@RequestParam("parentId") String parentId) {
        return departmentManager.listAllDepartmentBaseInfoByParentDepartmentId(Long.parseLong(parentId));
    }
}
