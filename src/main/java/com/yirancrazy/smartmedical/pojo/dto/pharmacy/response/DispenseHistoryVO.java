package com.yirancrazy.smartmedical.pojo.dto.pharmacy.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 药师端 - 发药历史列表项
 * @Author: YiRanCrazy@gmail.com
 * @Description: 已发药处方分页列表项，含发药人手机号与发药时间
 * @Datetime: 2026-09-09 12:00
 * @Version: 1.0
 */

@Data
@Schema(description = "发药历史列表项")
public class DispenseHistoryVO {

    @Schema(description = "处方ID")
    private Long prescriptionId;

    @Schema(description = "药品订单ID")
    private Long orderId;

    @Schema(description = "病历ID")
    private Long medicalRecordId;

    @Schema(description = "患者姓名")
    private String patientName;

    @Schema(description = "开方医生姓名")
    private String doctorName;

    @Schema(description = "发药人手机号")
    private String dispenserPhone;

    @Schema(description = "处方金额(分)")
    private Integer totalAmount;

    @Schema(description = "药品项数量")
    private Integer itemCount;

    @Schema(description = "发药时间")
    private LocalDateTime dispensedAt;
}
