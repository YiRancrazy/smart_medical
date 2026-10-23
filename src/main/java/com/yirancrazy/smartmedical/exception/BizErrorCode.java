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
    ACCOUNT_ALREADY_EXISTS(1005, "账号已存在"),
    REGISTRATION_ALREADY_EXISTS(1006, "该就诊人已挂号此排班"),
    MEDICAL_RECORD_ALREADY_SUBMITTED(2001, "病历已提交，不可修改"),
    MEDICAL_RECORD_NOT_FOUND(2002, "病历不存在"),
    DRUG_NOT_FOUND(3001, "药品不存在"),
    DRUG_INVENTORY_INSUFFICIENT(3002, "药品库存不足"),
    ORDER_AMOUNT_TOO_LARGE(3004, "金额超出上限"),
    INVENTORY_LOCKED(3003, "库存正在被其他操作锁定"),
    DRUG_PRICE_INVALID(3005, "药品价格未配置"),
    PRESCRIPTION_NOT_FOUND(4001, "处方不存在"),
    PRESCRIPTION_NOT_PAID(4002, "处方未支付"),
    PRESCRIPTION_NOT_OWNED(4003, "无权操作此处方"),
    PRESCRIPTION_ALREADY_DISPENSED(4004, "处方已发药"),
    PRESCRIPTION_ALREADY_CANCELLED(4005, "处方已取消"),
    ORDER_STATUS_INVALID(5001, "订单状态不允许此操作"),
    ORDER_NOT_OWNED(5002, "无权操作此订单"),
    DOCTOR_NOT_MATCH(6001, "医生不匹配"),
    CONCURRENT_OPERATION(7001, "操作冲突，请刷新后重试"),
    LOGIN_RATE_LIMITED(8001, "登录尝试过于频繁，请5分钟后再试"),
    PATIENT_DATA_INVALID(9001, "就诊人数据异常"),
    OPERATION_NOT_SUPPORTED(9002, "当前环境不支持此操作"),
    PATIENT_ID_CARD_CONFLICT(9003, "该身份证已存在患者档案"),
    PATIENT_ALREADY_OWNED(9004, "该患者已被其他账号绑定为本人"),
    PATIENT_RELATION_ALREADY_EXISTS(9005, "该用户已存在该就诊人"),
    PATIENT_PHONE_MISMATCH(9006, "本人关系手机号与患者电话不一致"),
    PATIENT_ACCOUNT_INVALID(9007, "当前账号缺少可用的患者角色账号"),
    PATIENT_ACCOUNT_AMBIGUOUS(9008, "当前用户存在多个有效患者角色账号"),
    PATIENT_SELF_RELATION_DELETE_FORBIDDEN(9009, "本人关系不能删除"),
    PATIENT_DEFAULT_REQUIRED(9010, "当前账户不能没有默认就诊人"),
    SMS_CODE_SEND_TOO_FREQUENT(8002, "验证码发送过于频繁，请稍后再试"),
    SMS_CODE_SEND_FAILED(8003, "验证码发送失败，请稍后再试"),
    SMS_CODE_EXPIRED(8004, "验证码已过期，请重新获取"),
    SMS_CODE_WRONG(8005, "验证码错误"),
    SMS_CODE_EXCEED_LIMIT(8006, "验证码错误次数过多，已失效，请重新获取");

    private final int code;
    private final String defaultMessage;
}
