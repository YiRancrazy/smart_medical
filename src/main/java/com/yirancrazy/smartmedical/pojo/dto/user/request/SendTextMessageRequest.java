package com.yirancrazy.smartmedical.pojo.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "文字内容")
    private String content;
}