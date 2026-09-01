package com.yirancrazy.smartmedical.pojo.dto.doctor.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "药品ID不能为空")
    @Schema(description = "药品ID")
    private Long drugId;

    @NotNull(message = "数量不能为空")
    @Schema(description = "数量")
    private Integer quantity;

    @NotBlank(message = "使用方法不能为空")
    @Schema(description = "使用方法")
    private String usageMethod;
}