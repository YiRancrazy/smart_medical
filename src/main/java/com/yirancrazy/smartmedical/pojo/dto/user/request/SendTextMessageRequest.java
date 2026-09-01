package com.yirancrazy.smartmedical.pojo.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 发送文字消息请求
 * @Datetime: 2026-07-21 20:30
 * @Version: 1.0
 */

@Data
@Schema(description = "发送文字消息请求")
public class SendTextMessageRequest {

    @NotNull(message = "医生ID不能为空")
    @Schema(description = "医生ID")
    private Long doctorId;

    @NotBlank(message = "文字内容不能为空")
    @Schema(description = "文字内容")
    private String content;
}