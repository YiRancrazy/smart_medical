package com.yirancrazy.smartmedical.pojo.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员挂号排班模板详情响应
 * @Datetime: 2026-03-20 19:27
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminRegistrationScheduleTemplateDetail {
    private String id;
    private String doctorId;
    private String doctorName;
    private String departmentId;
    private String departmentName;
    private String scheduleDate;
    private String scheduleType;
    private String startTime;
    private String endTime;
    private String remaining;
    private String total;
    private String address;
    private String status;
    private String remark;
}
