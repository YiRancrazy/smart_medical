package com.yirancrazy.smartmedical.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 定时任务常量（yml 前缀 smart-medical.scheduled-task）
 * @Version: 1.0
 * @DateTime: 2026/9/7 9:50
 **/

@Data
@Component
@ConfigurationProperties(prefix = "smart-medical.scheduled-task")
public class ScheduledTaskProperties {
    /** 订单超时扫描间隔（毫秒），默认 600000（10 分钟） */
    private long timeoutScanIntervalMs = 600_000L;

    /** 应用启动后首次执行超时扫描的延迟（毫秒），默认 600000 */
    private long initTimeoutScanIntervalMs = 600_000L;
}
