package com.yirancrazy.smartmedical.pojo.dto.user.response.admin.detail;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生详情响应
 * @Datetime: 2026-03-07 14:59
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDoctorDetailResponse {
    private String doctorId;            // 医生id
    private String doctorName;          // 医生名称
    private String departmentId;        // 科室id
    private String departmentName;      // 科室名称
    private String positionId;          // 职位id
    private String positionName;        // 职位名称
    private String degreeId;            // 学历id
    private String degreeName;          // 学历名称
    private String avatar;              // 头像
    private String address;             //  地址
    private String scope;               // 分数
    private List<String> tags;          // 标签
    private String description;         // 描述
    private String status;              // 状态
}