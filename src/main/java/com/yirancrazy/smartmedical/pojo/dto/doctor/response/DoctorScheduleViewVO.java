package com.yirancrazy.smartmedical.pojo.dto.doctor.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 医生排班视图项（日 / 周 / 月视图共用，纯排班信息，不含患者数据）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生排班视图项
 * @Datetime: 2026-09-06 10:00
 * @Version: 1.0
 */

@Data
@Schema(description = "医生排班视图项")
public class DoctorScheduleViewVO {

    @Schema(description = "排班模板ID")
    private String scheduleId;

    @Schema(description = "排班日期")
    private LocalDate scheduleDate;

    @Schema(description = "班次名称")
    private String shiftName;

    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "出诊地点")
    private String location;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态: 1-正常, 0-停诊")
    private Integer status;
}
