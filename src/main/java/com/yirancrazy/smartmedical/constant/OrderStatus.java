package com.yirancrazy.smartmedical.constant;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支付状态机
 * @Datetime: 2026-02-22 18:25
 * @Version: 1.0
 */

public enum OrderStatus {
    WAITING_FOR_PAYMENT("待支付", 0),
    PAID("已支付", 1),
    CANCELED("已取消", 2),
    FINISHED("已完成", 3),
    FAILED("支付失败", 4),
    REFUNDED("已退款", 6);

    private final String description;
    private final int code;

    OrderStatus(String description, int code) {
        this.description = description;
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }
}