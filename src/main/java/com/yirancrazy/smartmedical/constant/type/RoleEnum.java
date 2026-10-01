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

    ADMIN(1, "系统管理员", "admin"),
    DOCTOR(2, "医生", "doctor"),
    PATIENT(4, "患者", "user"),
    PHARMACIST(6, "药师", "pharmacy"),
    /** 非业务角色，仅用于系统自动写入的审计日志；code=0 为哨兵值 */
    SYSTEM(0, "系统", "system");

    private final Integer code;
    private final String name;
    /** 审计字段 operator_role 的存量字符串值，与历史数据保持一致 */
    private final String role;
}
