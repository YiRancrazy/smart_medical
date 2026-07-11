package com.yirancrazy.smartmedical.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-22 18:18
 * @Version: 1.0
 */

@AllArgsConstructor
@Getter
public enum RegistrationStatusEnum {
    WAITING_FOR_PAYMENT(0, "等待支付"), // 等待支付
    SUCCESS(1, "支付成功/待就诊"), // 支付成功/待就诊
    FAILED(2 , "支付失败"), // 支付失败
    CANCELED(3, "取消"), // 取消
    COMPLETED(4, "完成"),  // 完成
    REPORTED(5, "已报到"), // 已报到
    IN_TREATMENT(6, "就诊中"), // 就诊中
    PENDING_PAYMENT(7, "待支付"); // 就诊后待支付（处方/检查补缴）

    private final int code;
    private final String message;
}