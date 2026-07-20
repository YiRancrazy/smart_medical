package com.yirancrazy.smartmedical.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户患者关系类
 * @Datetime: 2026-03-01 16:30
 * @Version: 1.0
 */

@AllArgsConstructor
@Getter
public enum UserPatientRelationEnum {
    SELF(0,"self", "本人"),
    OTHER(1,"other", "其他");
    private final Integer code;
    private final String alias;
    private final String message;
}