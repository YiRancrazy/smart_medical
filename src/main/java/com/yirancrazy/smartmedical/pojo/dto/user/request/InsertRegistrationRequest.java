package com.yirancrazy.smartmedical.pojo.dto.user.request;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "排班ID不能为空")
    private String registrationScheduleId;        // 排班id

    private String userId;                           // 用户id

    @NotBlank(message = "就诊卡ID不能为空")
    private String patientCardId;                 // 患者卡id
}