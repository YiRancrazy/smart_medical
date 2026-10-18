package com.yirancrazy.smartmedical.pojo.dto.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端 - 病历详情
 * @Author: YiRanCrazy@gmail.com
 * @Description: 病历详情 VO（管理端查看）
 * @Datetime: 2026-07-25 20:10
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

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "患者ID")
    private Long patientId;

    @Schema(description = "患者姓名")
    private String patientName;

    @Schema(description = "患者手机号")
    private String patientPhone;

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
