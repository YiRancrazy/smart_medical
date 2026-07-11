package com.yirancrazy.smartmedical.constant.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 角色枚举
 * @Datetime: 2026-07-11 12:10
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum RoleEnum {

    USER(1, "用户"),
    ADMIN(2, "管理员"),
    DOCTOR(3, "医生");

    private final Integer code;
    private final String name;
}