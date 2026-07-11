package com.yirancrazy.smartmedical.controller.admin;

import com.yirancrazy.smartmedical.manager.DoctorManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminDoctorSimpleResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.detail.AdminDoctorDetailResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
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
    public Result<List<AdminDoctorSimpleResponse>> getDoctorById(String name) {
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
    public Result<PageResult<AdminDoctorDetailResponse>> listAllDoctorsSimpleResponseByUsernameAndDepartmentIdAndPage(String username, Long departmentId, Integer current, Integer size) {
        return doctorManager.listDoctorsSimpleResponseByLikeDoctorNameAndDepartmentIdAndPage( username, departmentId, current, size );
    }

}