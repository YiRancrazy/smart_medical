package com.yirancrazy.smartmedical.pojo.dto.doctor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 处方明细请求
 * @Author: YiRanCrazy@gmail.com
 * @Description: 处方明细请求
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "处方明细请求")
public class PrescriptionItemRequest {

    @Schema(description = "药品ID")
    private Long drugId;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "使用方法")
    private String usageMethod;
}