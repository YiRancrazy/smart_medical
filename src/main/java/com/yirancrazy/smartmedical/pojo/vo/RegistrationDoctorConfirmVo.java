package com.yirancrazy.smartmedical.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号确认页面医生信息返回对象
 * @Datetime: 2026-02-20 08:33
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDoctorConfirmVo {
    private String id;                              // 医生ID
    private String name;                          // 医生名称
    private String departmentId;                    // 科室ID
    private String departmentName;                // 科室名称
    private String avatar;                        // 头像
    private String doctorPositionId;                // 职位ID
    private String positionName;                  // 职位
}