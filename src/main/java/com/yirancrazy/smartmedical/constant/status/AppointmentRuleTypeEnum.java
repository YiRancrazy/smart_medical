package com.yirancrazy.smartmedical.constant.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 预约规则类型枚举
 * @Datetime: 2026-02-28 21:00
 * @Version: 1.0
 */

@AllArgsConstructor
@Getter
public enum AppointmentRuleTypeEnum {
    // 设置普通门诊0 专家门诊1 急诊2
    OUT_PATIENT(0, "普通门诊"),
    EXPERT_OUT_PATIENT(1, "专家门诊"),
    EMERGENCY(2, "急诊");

    private final Integer code;
    private final String message;

}