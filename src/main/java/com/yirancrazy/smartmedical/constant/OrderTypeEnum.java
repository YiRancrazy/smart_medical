package com.yirancrazy.smartmedical.constant;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 订单类型枚举（与 order_type 表主键保持一致）
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

public enum OrderTypeEnum {

    REGISTRATION(1L, "挂号订单"),
    DRUG(2L, "药品订单"),
    EXAMINATION(3L, "检查订单"),
    INPATIENT_DEPOSIT(4L, "住院押金"),
    PHYSICAL_EXAMINATION(5L, "体检订单");

    private final Long code;
    private final String name;

    OrderTypeEnum(Long code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 根据订单类型ID获取枚举
     */
    public static OrderTypeEnum getByCode(Long code) {
        if (code == null) {
            return null;
        }
        for (OrderTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    // Getters
    public Long getCode() { return code; }
    public String getName() { return name; }
}
