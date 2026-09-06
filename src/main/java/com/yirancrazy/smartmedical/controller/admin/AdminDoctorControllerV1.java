package com.yirancrazy.smartmedical.controller.admin;

import com.yirancrazy.smartmedical.manager.DoctorManager;
import com.yirancrazy.smartmedical.pojo.Degree;
import com.yirancrazy.smartmedical.pojo.DoctorPosition;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.admin.request.AdminDoctorAddRequest;
import com.yirancrazy.smartmedical.pojo.dto.admin.request.AdminDoctorUpdateRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminDoctorSimpleResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.detail.AdminDoctorDetailResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员端 - 医生管理接口
 * @Datetime: 2026-03-06 12:53
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@Tag(name = "管理员-医生管理")
@RequestMapping("/api/admin/v1/doctor")
public class AdminDoctorControllerV1 {
    private final DoctorManager doctorManager;

    /**
     * 根据ID查询医生信息
     * @param name 医生名称
     * @return 医生信息
     */
    @Operation(summary = "根据姓名查询医生信息")
    @GetMapping("/simple")
    public Result<List<AdminDoctorSimpleResponse>> getDoctorById(@RequestParam(required = false) String name) {
        return doctorManager.listDoctorsSimpleResponseByDoctorName( name );
    }


    /**
     * 多条件检索医生列表
     * @param username 医生姓名
     * @param departmentId 科室ID
     * @param current 当前页
     * @param size 每页数量
     * @return 医生列表
     */
    @Operation(summary = "多条件检索医生列表")
    @GetMapping("/detail/list")
    public Result<PageResult<AdminDoctorDetailResponse>> listAllDoctorsSimpleResponseByUsernameAndDepartmentIdAndPage(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return doctorManager.listDoctorsSimpleResponseByLikeDoctorNameAndDepartmentIdAndPage( username, departmentId, current, size );
    }

    /**
     * 编辑医生信息
     * @param id 医生ID
     * @param request 编辑请求
     * @return 影响行数
     */
    @Operation(summary = "管理员端 - 编辑医生信息")
    @PutMapping("/{id:\\d+}")
    public Result<Integer> updateDoctor(@PathVariable Long id, @Valid @RequestBody AdminDoctorUpdateRequest request) {
        return doctorManager.updateDoctor(id, request);
    }

    /**
     * 添加医生并创建登录账户
     * @param request 添加请求
     * @return 新医生 ID
     */
    @Operation(summary = "管理员端 - 添加医生")
    @PostMapping("/")
    public Result<Long> addDoctor(@Valid @RequestBody AdminDoctorAddRequest request) {
        return doctorManager.addDoctorWithAccount(request);
    }

    /**
     * 查询全部职称（表单下拉数据源）
     * @return 职称列表
     */
    @Operation(summary = "管理员端 - 查询全部医生职称")
    @GetMapping("/positions")
    public Result<List<DoctorPosition>> listAllDoctorPositions() {
        return doctorManager.listAllDoctorPositions();
    }

    /**
     * 查询全部学历（表单下拉数据源）
     * @return 学历列表
     */
    @Operation(summary = "管理员端 - 查询全部学历")
    @GetMapping("/degrees")
    public Result<List<Degree>> listAllDegrees() {
        return doctorManager.listAllDegrees();
    }

    /**
     * 上传医生头像
     * @param file 图片文件
     * @return 图片 URL
     */
    @Operation(summary = "管理员端 - 上传医生头像")
    @PostMapping("/upload/image")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            return Result.success(doctorManager.uploadAvatar(file));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("图片上传失败");
        }
    }

}