package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.DoctorManager;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.vo.DoctorVo;
import com.yirancrazy.smartmedical.pojo.vo.RegistrationDoctorBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.RegistrationDoctorConfirmVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/doctor")
@RequiredArgsConstructor
@Tag(name = "医生管理", description = "医生相关接口")
public class UserDoctorControllerV1 {

    private final DoctorManager doctorManager;

//    @PostMapping("/add")
//    @Operation(summary = "添加医生", description = "添加新医生")
//    public int addDoctor(@RequestBody Doctor doctor) {
//        return doctorManager.addDoctor(doctor);
//    }
//
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取医生", description = "根据医生ID获取医生信息")
    @Parameter(name = "id", description = "医生ID", required = true)
    public Result<DoctorVo
            > getDoctorById(@PathVariable String id) {
        return doctorManager.getDoctorById(Long.parseLong(id));
    }

    @GetMapping("/registration/baseInfo")
    @Operation(summary = "根据科室ID获取科室下医生挂号的基本信息", description = "根据科室ID获取科室下医生挂号的基本信息")
    @Parameter(name = "departmentId", description = "科室ID", required = true)
    public Result<List<RegistrationDoctorBaseInfo>> getRegistrationDoctorBaseInfoByDepartmentId(@RequestParam("departmentId") String departmentId){
        return doctorManager.getRegistrationDoctorBaseInfoByDepartmentId(Long.parseLong(departmentId));
    }


    @GetMapping("/registration/confirm")
    @Operation(summary = "获取挂号确认信息", description = "根据医生ID获取挂号确认信息")
    @Parameter(name = "doctorId", description = "医生ID", required = true)
    public Result<RegistrationDoctorConfirmVo> getRegistrationDoctorConfirmInfo(@RequestParam("doctorId") String doctorId){
        return doctorManager.getRegistrationDoctorConfirmInfo(Long.parseLong(doctorId));
    }
}
