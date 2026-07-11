package com.yirancrazy.smartmedical.pojo.dto.pharmacy.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 发药返回
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Data
@Schema(description = "发药返回")
public class DispenseVO {

    @Schema(description = "处方ID")
    private Long prescriptionId;

    @Schema(description = "处方状态")
    private Integer prescriptionStatus;

    @Schema(description = "发药时间")
    private LocalDateTime dispensedAt;

    @Schema(description = "发药明细")
    private List<DispenseItem> items;

    @Data
    @Schema(description = "发药明细项")
    public static class DispenseItem {

        @Schema(description = "药品ID")
        private Long drugId;

        @Schema(description = "药品名")
        private String drugName;

        @Schema(description = "发药数量")
        private Integer quantity;

        @Schema(description = "出库后库存数量")
        private Integer stockAfter;
    }
}