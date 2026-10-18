package com.yirancrazy.smartmedical.pojo.dto.doctor.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 医生端 - 处方详情 VO
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生端处方详情（含患者信息及药品明细）
 * @Datetime: 2026-07-24 19:00
 * @Version: 1.0
 */

@Data
@Schema(description = "医生端处方详情")
public class DoctorPrescriptionDetailVO {

    @Schema(description = "处方ID")
    private Long id;

    @Schema(description = "病历ID")
    private Long medicalRecordId;

    @Schema(description = "患者ID")
    private Long patientId;

    @Schema(description = "患者姓名")
    private String patientName;

    @Schema(description = "患者手机号")
    private String patientPhone;

    @Schema(description = "处方状态(0待支付 1已支付 2已发药 3已取消)")
    private Integer status;

    @Schema(description = "处方金额(分)")
    private Integer totalAmount;

    @Schema(description = "关联药品订单ID")
    private Long orderId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "处方明细")
    private List<PrescriptionItemVO> items;

    @Data
    @Schema(description = "处方明细项")
    public static class PrescriptionItemVO {

        @Schema(description = "药品ID")
        private Long drugId;

        @Schema(description = "药品通用名")
        private String commonName;

        @Schema(description = "规格")
        private String specification;

        @Schema(description = "单位")
        private String unit;

        @Schema(description = "单价(分)")
        private Integer unitPrice;

        @Schema(description = "数量")
        private Integer quantity;

        @Schema(description = "用法用量")
        private String usageMethod;
    }
}
