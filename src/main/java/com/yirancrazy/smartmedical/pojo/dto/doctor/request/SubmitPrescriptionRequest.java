package com.yirancrazy.smartmedical.pojo.dto.doctor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 提交病历 + 开处方请求
 * @Author: YiRanCrazy@gmail.com
 * @Description: 提交病历 + 开处方请求
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Data
@Schema(description = "提交病历 + 开处方请求")
public class SubmitPrescriptionRequest {

    @Schema(description = "挂号记录ID")
    private Long registrationId;

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

    @Schema(description = "处方药品列表")
    private List<PrescriptionItemRequest> items;
}