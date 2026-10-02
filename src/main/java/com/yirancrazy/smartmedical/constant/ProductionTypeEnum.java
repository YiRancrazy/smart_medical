package com.yirancrazy.smartmedical.constant;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 项目类型枚举（与 production_type 表主键保持一致）
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

public enum ProductionTypeEnum {

    DRUG(1L, "药品"),
    EXAMINATION(2L, "检查"),
    TREATMENT(3L, "治疗"),
    SURGERY(4L, "手术"),
    MATERIAL(5L, "材料"),
    REGISTRATION(6L, "挂号");

    private final Long code;
    private final String name;

    ProductionTypeEnum(Long code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据项目类型ID获取枚举
     */
    public static ProductionTypeEnum getByCode(Long code) {
        if (code == null) {
            return null;
        }
        for (ProductionTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public Long getCode() { return code; }
    public String getName() { return name; }
}
