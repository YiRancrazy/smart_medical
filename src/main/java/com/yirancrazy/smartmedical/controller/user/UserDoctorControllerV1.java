package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.DoctorManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.vo.DoctorVo;
import com.yirancrazy.smartmedical.pojo.vo.RegistrationDoctorBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.RegistrationDoctorConfirmVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 医生与挂号医生查询接口
 * @Datetime: 2026-02-02 13:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/doctor")
@RequiredArgsConstructor
@Tag(name = "医生管理", description = "医生相关接口")
public class UserDoctorControllerV1 {

    private final DoctorManager doctorManager;

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "根据ID获取医生", description = "根据医生ID获取医生信息")
    @Parameter(name = "id", description = "医生ID", required = true)
    public Result<DoctorVo> getDoctorById(@PathVariable Long id) {
        return doctorManager.getDoctorById(id);
    }

    @GetMapping("/registration/baseInfo")
    @Operation(summary = "根据科室ID获取科室下医生挂号的基本信息", description = "根据科室ID获取科室下医生挂号的基本信息")
    @Parameter(name = "departmentId", description = "科室ID", required = true)
    public Result<List<RegistrationDoctorBaseInfo>> getRegistrationDoctorBaseInfoByDepartmentId(@RequestParam("departmentId") Long departmentId){
        return doctorManager.getRegistrationDoctorBaseInfoByDepartmentId(departmentId);
    }


    @GetMapping("/list")
    @Operation(summary = "分页查询医生", description = "分页查询医生列表")
    public Result<?> listDoctorsByPage(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
        return doctorManager.listDoctorsSimpleResponseByLikeDoctorNameAndDepartmentIdAndPage(name, departmentId, pageNum, pageSize);
    }

    @GetMapping("/detail/{id:\\d+}")
    @Operation(summary = "医生详情", description = "根据医生ID获取医生详情")
    public Result<DoctorVo> getDoctorDetail(@PathVariable("id") Long id) {
        return doctorManager.getDoctorById(id);
    }

    @GetMapping("/registration/confirm")
    @Operation(summary = "获取挂号确认信息", description = "根据医生ID获取挂号确认信息")
    @Parameter(name = "doctorId", description = "医生ID", required = true)
    public Result<RegistrationDoctorConfirmVo> getRegistrationDoctorConfirmInfo(@RequestParam("doctorId") Long doctorId){
        return doctorManager.getRegistrationDoctorConfirmInfo(doctorId);
    }
}
