package com.yirancrazy.smartmedical.pojo.dto.doctor.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医生排班列表项
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生排班列表项
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Data
@Schema(description = "医生排班列表项")
public class DoctorScheduleVO {

    @Schema(description = "挂号记录ID")
    private Long registrationId;

    @Schema(description = "挂号状态")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;
}