package com.yirancrazy.smartmedical.controller.doctor;

import com.yirancrazy.smartmedical.manager.MedicalRecordManager;
import com.yirancrazy.smartmedical.manager.PrescriptionManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.DraftMedicalRecordRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.SubmitPrescriptionRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.MedicalRecordDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.PrescriptionSubmitVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生端 - 病历/开方
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Tag(name = "医生端 - 病历")
@RestController
@RequestMapping("/api/doctor/v1/medical-record")
@RequiredArgsConstructor
public class DoctorMedicalRecordControllerV1 {

    private final MedicalRecordManager medicalRecordManager;
    private final PrescriptionManager prescriptionManager;

    /**
     * 医生端 - 取病历(按挂号ID)
     * @param regId 挂号记录ID
     * @param doctorId 当前医生ID
     * @return 病历详情 VO
     */
    @Operation(summary = "医生端 - 取病历(按挂号ID)")
    @GetMapping("/registration/{regId}")
    public Result<MedicalRecordDetailVO> getByRegistration(@PathVariable Long regId,
                                                           @RequestAttribute("currentDoctorId") Long doctorId) {
        return Result.success(medicalRecordManager.getByRegistrationId(regId, doctorId));
    }

    /**
     * 医生端 - 保存病历草稿
     * @param req 病历草稿请求
     * @param doctorId 当前医生ID
     */
    @Operation(summary = "医生端 - 保存病历草稿")
    @PostMapping("/draft")
    public Result<Void> saveDraft(@RequestBody DraftMedicalRecordRequest req,
                                  @RequestAttribute("currentDoctorId") Long doctorId) {
        medicalRecordManager.draft(req, doctorId);
        return Result.success(null);
    }

    /**
     * 医生端 - 提交病历+开处方
     * @param req 提交病历+开处方请求
     * @param doctorId 医生ID
     * @return 提交结果
     */
    @Operation(summary = "医生端 - 提交病历+开处方")
    @PostMapping("/submit")
    public Result<PrescriptionSubmitVO> submit(@RequestBody SubmitPrescriptionRequest req,
                                               @RequestAttribute("currentDoctorId") Long doctorId) {
        return Result.success(prescriptionManager.submit(req.getRegistrationId(), req, doctorId));
    }
}