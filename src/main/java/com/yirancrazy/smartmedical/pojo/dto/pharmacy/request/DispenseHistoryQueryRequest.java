package com.yirancrazy.smartmedical.pojo.dto.pharmacy.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;

/**
 * 药师端 - 发药历史分页查询请求
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支持按患者姓名、发药人手机号、处方/订单ID、发药日期范围分页查询已发药处方
 * @Datetime: 2026-09-09 12:00
 * @Version: 1.0
 */

@Data
@Schema(description = "发药历史查询请求")
public class DispenseHistoryQueryRequest {

    @Schema(description = "患者姓名（模糊）")
    private String patientName;

    @Schema(description = "发药人手机号（模糊）")
    private String dispenserPhone;

    @Schema(description = "处方ID（精确）")
    private Long prescriptionId;

    @Schema(description = "订单ID（精确）")
    private Long orderId;

    @Schema(description = "发药日期-开始")
    private LocalDate startDate;

    @Schema(description = "发药日期-结束")
    private LocalDate endDate;

    @Schema(description = "页码，默认 1")
    @Min(value = 1, message = "页码不能小于 1")
    private Integer pageNum = 1;

    @Schema(description = "每页条数，默认 10")
    @Min(value = 1, message = "每页条数不能小于 1")
    private Integer pageSize = 10;
}
