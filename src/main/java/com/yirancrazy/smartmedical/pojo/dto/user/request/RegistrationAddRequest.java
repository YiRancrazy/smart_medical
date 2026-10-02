package com.yirancrazy.smartmedical.pojo.dto.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 新增挂号记录请求
 * @Datetime: 2026-10-02 12:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationAddRequest {

    private Long userId;

    private Long registrationScheduleTemplateId;

    private Long orderId;

    private Integer status;

    private LocalDateTime registrationTime;

    private LocalDateTime checkInTime;

    private LocalDateTime visitStartTime;

    private LocalDateTime visitEndTime;
}
