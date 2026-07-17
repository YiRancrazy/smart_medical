package com.yirancrazy.smartmedical.constant.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 角色枚举（主要业务角色）
 * @Datetime: 2026-07-11 12:10
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum RoleEnum {

    ADMIN(1, "系统管理员"),
    DOCTOR(2, "医生"),
    PATIENT(4, "患者"),
    PHARMACIST(6, "药师");

    private final Integer code;
    private final String name;
}