package com.yirancrazy.smartmedical.constant.type;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-18 19:34
 * @Version: 1.0
 */

@AllArgsConstructor
@Getter
public enum RegistrationShiftTypeEnum {
    MORNING(0,"上午"),
    AFTERNOON(1,"下午"),
    NIGHT(2,"晚上"),
    MIDNIGHT(3,"加班"),
    OTHER(4,"节假日");

    private final Integer code;
    private final String message;

    public static Integer getCodeByName(String name) {
        for (RegistrationShiftTypeEnum type : values()) {
            System.out.println(type.name());
            if (type.getMessage().equals(name)) {
                return type.code;
            }
        }
        return null;
    }

}