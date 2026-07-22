package com.yirancrazy.smartmedical.pojo.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 病历列表项
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */

@Data
@Schema(description = "病历列表项")
public class MedicalRecordListVO {
    @Schema(description = "病历ID")
    private Long id;

    @Schema(description = "挂号记录ID")
    private Long registrationId;

    @Schema(description = "患者姓名")
    private String patientName;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "初步诊断")
    private String diagnosis;

    @Schema(description = "就诊日期")
    private LocalDateTime visitDate;

    @Schema(description = "关联处方ID")
    private Long prescriptionId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}