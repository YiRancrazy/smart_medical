package com.yirancrazy.smartmedical.pojo.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.webmvc.core.fn.SpringdocRouteBuilder;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 预约信息简单响应
 * @Datetime: 2026-02-25 20:56
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponseSimple {
    private String id;                          // 挂号id
    private String orderId;                     // 关联订单id
    private Integer status;                     // 挂号状态
    private String scheduleDate;                // 预约日期
    private String scheduleTime;                // 预约开始时间
    private String doctorId;                    // 医生id
    private String doctorName;                  // 医生名称
    private String doctorAvatar;                // 医生头像
    private String doctorPosition;              // 医生职位
    private String departmentId;                // 科室id
    private String departmentName;              // 科室名称
    private String patientName;                 // 用户名称
    private Double registrationPrice;           // 挂号价格

}