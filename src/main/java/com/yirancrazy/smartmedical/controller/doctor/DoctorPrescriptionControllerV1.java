package com.yirancrazy.smartmedical.controller.doctor;

import com.yirancrazy.smartmedical.manager.PrescriptionManager;
import com.yirancrazy.smartmedical.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生端 - 处方作废
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Tag(name = "医生端 - 处方")
@RestController
@RequestMapping("/doctor/v1/prescription")
@RequiredArgsConstructor
public class DoctorPrescriptionControllerV1 {

    private final PrescriptionManager prescriptionManager;

    /**
     * 医生端 - 作废处方(仅待支付)
     * @param id 处方ID
     * @param doctorId 医生ID
     */
    @Operation(summary = "医生端 - 作废处方(仅待支付)")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @RequestParam Long doctorId) {
        prescriptionManager.cancelByDoctor(id, doctorId);
        return Result.success(null);
    }
}