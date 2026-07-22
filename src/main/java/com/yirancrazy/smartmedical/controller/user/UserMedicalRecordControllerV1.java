package com.yirancrazy.smartmedical.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yirancrazy.smartmedical.manager.MedicalRecordManager;
import com.yirancrazy.smartmedical.manager.PatientManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.MedicalRecordListVO;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 病历查询
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */

@Tag(name = "用户端 - 病历", description = "我的病历查询")
@RestController
@RequestMapping("api/user/v1/medical-record")
@RequiredArgsConstructor
public class UserMedicalRecordControllerV1 {

    private final MedicalRecordService medicalRecordService;
    private final PatientManager patientManager;
    private final MedicalRecordManager medicalRecordManager;

    @Operation(summary = "用户端 - 我的病历列表")
    @GetMapping("/list")
    @Parameter(name = "patientCardId", description = "就诊卡ID", required = false)
    public Result<List<MedicalRecordListVO>> list(@RequestAttribute("currentUserId") Long userId,
                                                  @RequestParam(required = false) Long patientCardId) {
        List<Long> patientUserIds = patientManager.getAccessiblePatientUserIds(userId, patientCardId);
        if (patientUserIds.isEmpty()) {
            return Result.success(Collections.emptyList());
        }
        List<MedicalRecord> records = medicalRecordService.list(
                new LambdaQueryWrapper<MedicalRecord>()
                        .in(MedicalRecord::getPatientId, patientUserIds)
                        .orderByDesc(MedicalRecord::getCreateTime));
        return Result.success(medicalRecordManager.toListVOs(records));
    }

    @Operation(summary = "用户端 - 病历详情")
    @Parameter(name = "id", description = "病历ID", required = true)
    @GetMapping("/{id:\\d+}")
    public Result<MedicalRecord> detail(@PathVariable Long id,
                                       @RequestAttribute("currentUserId") Long userId) {
        MedicalRecord record = medicalRecordService.getById(id);
        if (record == null || !userId.equals(record.getPatientId())) {
            return Result.fail("无权查看该病历");
        }
        return Result.success(record);
    }
}