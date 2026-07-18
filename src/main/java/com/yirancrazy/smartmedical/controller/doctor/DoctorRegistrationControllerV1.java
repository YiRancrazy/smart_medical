package com.yirancrazy.smartmedical.controller.doctor;

import com.yirancrazy.smartmedical.manager.DoctorManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.WaitingPatientVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生端 - 挂号/叫号
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Tag(name = "医生端 - 挂号/叫号")
@RestController
@RequestMapping("/api/doctor/v1/registration")
@RequiredArgsConstructor
public class DoctorRegistrationControllerV1 {

    private final DoctorManager doctorManager;

    /**
     * 医生端 - 待叫号列表（status=REPORTED）
     * @param doctorId 医生ID（JWT 自动注入）
     */
    @Operation(summary = "医生端 - 待叫号列表")
    @GetMapping("/waiting")
    public Result<List<WaitingPatientVO>> waiting(@RequestAttribute("currentDoctorId") Long doctorId) {
        return Result.success(doctorManager.listWaiting(doctorId));
    }

    /**
     * 医生端 - 就诊中列表（status=IN_TREATMENT 或 PENDING_PAYMENT）
     * @param doctorId 医生ID（JWT 自动注入）
     */
    @Operation(summary = "医生端 - 就诊中列表")
    @GetMapping("/in-progress")
    public Result<List<WaitingPatientVO>> inProgress(@RequestAttribute("currentDoctorId") Long doctorId) {
        return Result.success(doctorManager.listInProgress(doctorId));
    }

    /**
     * 医生端 - 叫号接诊
     * @param regId 挂号记录ID
     * @param doctorId 医生ID（JWT 自动注入）
     */
    @Operation(summary = "医生端 - 叫号接诊")
    @PostMapping("/{id}/call")
    public Result<Void> call(@PathVariable("id") Long regId,
                             @RequestAttribute("currentDoctorId") Long doctorId) {
        doctorManager.callPatient(regId, doctorId);
        return Result.success(null);
    }
}
