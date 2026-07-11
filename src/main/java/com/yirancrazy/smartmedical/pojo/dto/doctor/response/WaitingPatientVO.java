package com.yirancrazy.smartmedical.pojo.dto.doctor.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 待叫号列表项
 * @Author: YiRanCrazy@gmail.com
 * @Description: 待叫号列表项
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Data
@Schema(description = "待叫号列表项")
public class WaitingPatientVO {

    @Schema(description = "挂号记录ID")
    private Long registrationId;

    @Schema(description = "患者ID")
    private Long patientId;

    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;
}