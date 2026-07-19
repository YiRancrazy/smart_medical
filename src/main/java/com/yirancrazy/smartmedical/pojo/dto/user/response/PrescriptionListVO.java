package com.yirancrazy.smartmedical.pojo.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户端 - 处方列表项 VO
 * @Author: YiRanCrazy@gmail.com
 * @Description: 我的处方列表卡片数据
 * @Datetime: 2026-07-19 21:00
 * @Version: 1.0
 */

@Data
@Schema(description = "处方列表项")
public class PrescriptionListVO {

    @Schema(description = "处方ID")
    private Long id;

    @Schema(description = "病历ID")
    private Long medicalRecordId;

    @Schema(description = "处方金额(分)")
    private Integer totalAmount;

    @Schema(description = "处方状态(0待支付 1已支付 2已发药 3已取消)")
    private Integer status;

    @Schema(description = "药品项数量")
    private Integer itemCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
