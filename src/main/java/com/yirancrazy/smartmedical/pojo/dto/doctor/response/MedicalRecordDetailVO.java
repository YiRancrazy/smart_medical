package com.yirancrazy.smartmedical.pojo.dto.doctor.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 病历详情 VO
 * @Author: YiRanCrazy@gmail.com
 * @Description: 病历详情 VO
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Data
@Schema(description = "病历详情")
public class MedicalRecordDetailVO {

    @Schema(description = "病历ID")
    private Long id;

    @Schema(description = "挂号记录ID")
    private Long registrationId;

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "患者ID")
    private Long patientId;

    @Schema(description = "主诉")
    private String chiefComplaint;

    @Schema(description = "现病史")
    private String presentIllness;

    @Schema(description = "既往史")
    private String pastHistory;

    @Schema(description = "查体")
    private String physicalExam;

    @Schema(description = "诊断")
    private String diagnosis;

    @Schema(description = "治疗方案")
    private String treatmentPlan;

    @Schema(description = "状态: 0-草稿 1-已提交")
    private Integer status;
}