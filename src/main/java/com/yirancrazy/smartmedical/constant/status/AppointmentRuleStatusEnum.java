package com.yirancrazy.smartmedical.constant.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 预约规则状态枚举
 * @Datetime: 2026-02-28 19:54
 * @Version: 1.0
 */

@AllArgsConstructor
@Getter
public enum AppointmentRuleStatusEnum {
    NORMAL(1, "启用"),
    DELETED(2, "停用");

    private final Integer code;
    private final String message;
}