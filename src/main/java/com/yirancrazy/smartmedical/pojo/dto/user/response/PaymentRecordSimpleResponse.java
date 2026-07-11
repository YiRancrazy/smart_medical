package com.yirancrazy.smartmedical.pojo.dto.user.response;

import cn.hutool.core.date.DateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 预约信息简单响应
 * @Datetime: 2026-02-26 06:06
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecordSimpleResponse {
    private String paymentRecordId;               // 缴费记录id
    private LocalDateTime paymentTime;                   // 缴费时间
    private String orderId;
    private String orderTypeId;                   // 订单类型id
    private String orderType;                     // 订单类型名称
    private List<String> orderItems;               // 预约项目
    private String paymentMethodId;        // 预约方式id
    private String paymentMethodName;      // 预约方式名称
    private String paymentStatus;  // 支付状态
    private String paymentAmount;  // 支付金额
}