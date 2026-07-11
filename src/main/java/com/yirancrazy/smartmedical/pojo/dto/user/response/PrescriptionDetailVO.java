package com.yirancrazy.smartmedical.pojo.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 处方详情 VO
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */

@Data
@Schema(description = "处方详情")
public class PrescriptionDetailVO {
    @Schema(description = "处方ID")
    private Long id;

    @Schema(description = "处方状态(0待支付 1已支付 2已发药 3已取消)")
    private Integer status;

    @Schema(description = "处方金额(分)")
    private Integer totalAmount;

    @Schema(description = "关联药品订单ID")
    private Long orderId;

    @Schema(description = "处方明细")
    private List<PrescriptionItemVO> items;

    @Data
    @Schema(description = "处方明细项")
    public static class PrescriptionItemVO {
        @Schema(description = "药品ID")
        private Long drugId;

        @Schema(description = "数量")
        private Integer quantity;

        @Schema(description = "用法用量")
        private String usageMethod;
    }
}