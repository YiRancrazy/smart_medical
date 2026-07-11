package com.yirancrazy.smartmedical.pojo.dto.doctor.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 提交处方返回
 * @Author: YiRanCrazy@gmail.com
 * @Description: 提交处方返回
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Data
@Schema(description = "提交处方返回")
public class PrescriptionSubmitVO {

    @Schema(description = "病历ID")
    private Long medicalRecordId;

    @Schema(description = "处方ID")
    private Long prescriptionId;

    @Schema(description = "药品订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderSn;

    @Schema(description = "总金额(分)")
    private Integer totalAmount;

    @Schema(description = "挂号记录状态")
    private Integer registrationStatus;
}