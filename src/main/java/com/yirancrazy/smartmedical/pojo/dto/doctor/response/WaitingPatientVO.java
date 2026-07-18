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

    @Schema(description = "患者姓名")
    private String patientName;

    @Schema(description = "患者手机号")
    private String patientPhone;

    @Schema(description = "挂号状态")
    private Integer status;

    @Schema(description = "签到时间")
    private LocalDateTime checkInTime;

    @Schema(description = "挂号时间")
    private LocalDateTime registrationTime;
}