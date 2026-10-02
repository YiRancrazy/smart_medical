package com.yirancrazy.smartmedical.constant.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 角色枚举（业务角色与操作人别名）
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
public enum RoleEnum {

    ADMIN(1, "系统管理员", "admin", "admin"),
    DOCTOR(2, "医生", "doctor", "doctor"),
    PATIENT(4, "患者", "user", "user"),
    PHARMACIST(6, "药师", "pharmacist", "pharmacist"),
    /** 非业务角色，仅用于系统自动写入的审计日志；code=0 为哨兵值 */
    SYSTEM(0, "系统", "system", null);

    private final Integer code;
    private final String name;
    /** 审计字段 operator_role 的存量字符串值，与历史数据保持一致 */
    private final String role;
    /** Spring Security 权限名，由 hasRole / hasAnyRole 自动补 ROLE_ 前缀 */
    private final String securityRole;
    /**
     * 审计代码中沿用的别名。
     */
    public String getAlias() {
        return role;
    }

    /**
     * 转换为 Long 类型角色ID。
     */
    public Long getCodeAsLong() {
        return code.longValue();
    }

    /**
     * 根据角色ID获取枚举。
     */
    public static RoleEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RoleEnum role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}
