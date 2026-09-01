package com.yirancrazy.smartmedical.pojo.dto.user.request;

import jakarta.validation.constraints.NotNull;
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
    private Long paymentMethodId;               // 支付方式id

    @NotNull(message = "排班ID不能为空")
    private Long registrationScheduleId;        // 排班id

    private Long userId;                           // 用户id

    @NotNull(message = "就诊卡ID不能为空")
    private Long patientCardId;                 // 患者卡id
}