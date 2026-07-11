package com.yirancrazy.smartmedical.pojo.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 病历列表项
 * @Datetime: 2026-07-11 12:00
 * @Version: 1.0
 */

@Data
@Schema(description = "病历列表项")
public class MedicalRecordListVO {
    @Schema(description = "病历ID")
    private Long id;

    @Schema(description = "挂号记录ID")
    private Long registrationId;

    @Schema(description = "初步诊断")
    private String diagnosis;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}