package com.yirancrazy.smartmedical.pojo.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-06 12:56
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDoctorSimpleResponse {
    private String doctorId;   // 医生id
    private String doctorName; // 医生名称
    private String departmentId; // 科室id
    private String departmentName; // 科室名称
}