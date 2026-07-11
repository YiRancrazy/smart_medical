package com.yirancrazy.smartmedical.pojo.dto.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-25 18:34
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InsertRegistrationRequest {
    private String paymentMethodId;               // 支付方式id
    private String registrationScheduleId;        // 排班id
    private String userId;                           // 用户id
    private String patientCardId;                 // 患者卡id
}