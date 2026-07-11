package com.yirancrazy.smartmedical.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 缴费记录简单响应VO
 * @Datetime: 2026-03-01
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecordSimpleResponse {
    private String paymentId;        // 缴费记录ID
    private String orderNo;          // 订单号
    private String paymentType;      // 缴费类型（挂号费、检查费、药费、住院费等）
    private Integer paymentAmount;   // 缴费金额
    private String hospitalName;     // 医院名称
    private String departmentName;   // 科室名称
    private String doctorName;       // 医生姓名（挂号费时使用）
    private String paymentItem;      // 缴费项目描述
    private String paymentTime;      // 缴费时间（格式：yyyy-MM-dd HH:mm:ss）
    private Integer paymentStatus;   // 支付状态（0-待支付，1-支付成功，2-支付失败，3-已退款）
    private String paymentMethod;    // 支付方式（微信、支付宝、银行卡、现金等）
    private String patientName;      // 就诊人姓名
    private String patientIdCard;    // 就诊人身份证号（脱敏）
    private String receiptUrl;       // 发票URL（可选）
    private String remark;           // 备注（可选）
}
