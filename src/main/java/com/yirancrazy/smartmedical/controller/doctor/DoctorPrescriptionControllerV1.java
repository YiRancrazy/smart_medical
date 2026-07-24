package com.yirancrazy.smartmedical.controller.doctor;

import com.yirancrazy.smartmedical.manager.PrescriptionManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.DoctorPrescriptionDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.DoctorPrescriptionListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生端 - 处方查询/作废
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Tag(name = "医生端 - 处方")
@RestController
@RequestMapping("/api/doctor/v1/prescription")
@RequiredArgsConstructor
public class DoctorPrescriptionControllerV1 {

    private final PrescriptionManager prescriptionManager;

    /**
     * 医生端 - 处方列表
     * @param doctorId 当前医生ID
     * @return 当前医生的处方列表
     */
    @Operation(summary = "医生端 - 处方列表")
    @GetMapping("/list")
    public Result<List<DoctorPrescriptionListVO>> list(@RequestAttribute("currentDoctorId") Long doctorId) {
        return Result.success(prescriptionManager.listDoctorPrescriptions(doctorId));
    }

    /**
     * 医生端 - 处方详情
     * @param id 处方ID
     * @param doctorId 当前医生ID
     * @return 处方详情
     */
    @Operation(summary = "医生端 - 处方详情")
    @Parameter(name = "id", description = "处方ID", required = true)
    @GetMapping("/{id:\\d+}")
    public Result<DoctorPrescriptionDetailVO> detail(@PathVariable Long id,
                                                     @RequestAttribute("currentDoctorId") Long doctorId) {
        return Result.success(prescriptionManager.getDoctorPrescriptionDetail(id, doctorId));
    }

    /**
     * 医生端 - 作废处方(仅待支付)
     * @param id 处方ID
     * @param doctorId 医生ID
     */
    @Operation(summary = "医生端 - 作废处方(仅待支付)")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestAttribute("currentDoctorId") Long doctorId) {
        prescriptionManager.cancelByDoctor(id, doctorId);
        return Result.success(null);
    }
}