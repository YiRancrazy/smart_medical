package com.yirancrazy.smartmedical.constant;

/**
 * 订单类型常量（对应 DDL order_type 表 ID，与 order_type_sn 不同）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 订单类型常量（order_type.id）
 * @Datetime: 2026-09-03 09:00
 * @Version: 1.0
 */

public final class OrderTypeConstant {

    private OrderTypeConstant() {
    }

    /** 门诊挂号订单（order_type.id=1） */
    public static final long REGISTRATION = 1L;
    /** 药品订单（order_type.id=2） */
    public static final long DRUG = 2L;
}