package com.yirancrazy.smartmedical.constant.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-18 19:16
 * @Version: 1.0
 */

@AllArgsConstructor
@Getter
public enum RegistrationTypeEnum {
    SIMPLE(0,"普通挂号"),
    // 专家号
    EXPERT(1,"专家号"),
    // 外院专家号
    OUTSIDE_EXPERT(2,"外院专家号");
    private final Integer code;
    private final String message;
}