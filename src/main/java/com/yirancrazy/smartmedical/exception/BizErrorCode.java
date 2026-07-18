package com.yirancrazy.smartmedical.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 业务异常错误码
 * @Author: YiRanCrazy@gmail.com
 * @Description: 业务异常枚举，所有业务异常都引用此处
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Getter
@AllArgsConstructor
public enum BizErrorCode {

    REGISTRATION_NOT_FOUND(1001, "挂号记录不存在"),
    REGISTRATION_STATUS_INVALID(1002, "当前挂号状态不可操作"),
    REGISTRATION_NOT_OWNED(1003, "无权操作此挂号"),
    SCHEDULE_TEMPLATE_NOT_FOUND(1004, "挂号排班模板不存在"),
    MEDICAL_RECORD_ALREADY_SUBMITTED(2001, "病历已提交，不可修改"),
    MEDICAL_RECORD_NOT_FOUND(2002, "病历不存在"),
    DRUG_NOT_FOUND(3001, "药品不存在"),
    DRUG_INVENTORY_INSUFFICIENT(3002, "药品库存不足"),
    INVENTORY_LOCKED(3003, "库存正在被其他操作锁定"),
    PRESCRIPTION_NOT_FOUND(4001, "处方不存在"),
    PRESCRIPTION_NOT_PAID(4002, "处方未支付"),
    PRESCRIPTION_NOT_OWNED(4003, "无权操作此处方"),
    PRESCRIPTION_ALREADY_DISPENSED(4004, "处方已发药"),
    PRESCRIPTION_ALREADY_CANCELLED(4005, "处方已取消"),
    ORDER_STATUS_INVALID(5001, "订单状态不允许此操作"),
    DOCTOR_NOT_MATCH(6001, "医生不匹配"),
    CONCURRENT_OPERATION(7001, "操作冲突，请刷新后重试");

    private final int code;
    private final String defaultMessage;
}