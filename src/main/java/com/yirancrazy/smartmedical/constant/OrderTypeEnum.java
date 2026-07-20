package com.yirancrazy.smartmedical.constant;

import com.yirancrazy.smartmedical.pojo.OrderType;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 缴费单类型枚举
 * @Datetime: 2026-02-22 16:59
 * @Version: 1.0
 */

public enum OrderTypeEnum {

    OUTPATIENT_REGISTRATION("3110236876932865049", "门诊挂号单"),
    EXAMINATION_ORDER("0889228332644175818", "检查检验单"),
    PRESCRIPTION_ORDER("0657962839979386732", "药品处方单"),
    INPATIENT_PAYMENT("2247797221752133703", "住院缴费单"),
    SURGERY_APPOINTMENT("1289740951459473931", "手术预约单");

    private final String orderTypeSn;
    private final String name;

    OrderTypeEnum(String orderTypeSn, String name) {
        this.orderTypeSn = orderTypeSn;
        this.name = name;
    }

    // 根据code获取枚举
    public static OrderTypeEnum getByOrderTypeSn(String orderTypeSn) {
        for (OrderTypeEnum type : values()) {
            if (type.getOrderTypeSn().equals(orderTypeSn)) {
                return type;
            }
        }
        return null;
    }

    // Getters
    public String getOrderTypeSn() { return orderTypeSn; }
    public String getName() { return name; }
}