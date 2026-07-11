package com.yirancrazy.smartmedical.pojo.vo.registration.confirm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-20 14:14
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationConfirmTime {
    private String registrationScheduleId; //挂号排班id
    private LocalDateTime startTime; //开始时间
    private LocalDateTime endTime; //结束时间
    private Integer remainQuota; //剩余配额
    private Boolean available; //是否可用
}
