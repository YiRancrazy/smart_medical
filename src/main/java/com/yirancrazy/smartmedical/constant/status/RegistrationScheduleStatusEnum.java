package com.yirancrazy.smartmedical.constant.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 排班状态枚举
 * @Author: YiRanCrazy@gmail.com
 * @Description: 排班状态：1-正常 2-满号
 * @Datetime: 2026-09-05 20:30
 * @Version: 1.0
 */

@AllArgsConstructor
@Getter
public enum RegistrationScheduleStatusEnum {
    NORMAL(1, "正常"),
    FULL(2, "满号");

    private final Integer code;
    private final String message;
}