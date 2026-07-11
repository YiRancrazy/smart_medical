package com.yirancrazy.smartmedical.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 处方状态
 * @Author: YiRanCrazy@gmail.com
 * @Description: 处方状态机：0-待支付 → 1-已支付 → 2-已发药；任意状态可走 → 3-已取消
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Getter
@AllArgsConstructor
public enum PrescriptionStatus {

    PENDING_PAYMENT(0, "待支付"),
    PAID(1, "已支付"),
    DISPENSED(2, "已发药"),
    CANCELLED(3, "已取消");

    private final int code;
    private final String message;
}
