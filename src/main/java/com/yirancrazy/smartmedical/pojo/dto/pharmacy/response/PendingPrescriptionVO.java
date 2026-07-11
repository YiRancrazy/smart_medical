package com.yirancrazy.smartmedical.pojo.dto.pharmacy.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 待发药列表项
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Data
@Schema(description = "待发药列表项")
public class PendingPrescriptionVO {

    @Schema(description = "处方ID")
    private Long prescriptionId;

    @Schema(description = "药品订单ID")
    private Long orderId;

    @Schema(description = "患者ID")
    private Long patientId;

    @Schema(description = "挂号记录ID")
    private Long registrationSn;

    @Schema(description = "处方总金额(分)")
    private Integer totalAmount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}