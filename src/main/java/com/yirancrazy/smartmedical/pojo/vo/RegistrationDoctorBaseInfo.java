package com.yirancrazy.smartmedical.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号医生的基本信息
 * @Datetime: 2026-02-18 14:39
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDoctorBaseInfo {
    private String doctorId;                         // 医生ID
    private String doctorName;                         // 医生名称
    private String departmentId;                         // 科室ID
    private String departmentName;                         // 科室名称
    List<String> tags;                               // 标签
    private String description;                           // 描述
    private String avatar;                           // 头像
    private String position;                         // 职位
    private LocalDateTime recentWorkTime;             // 最近工作时间
    private double score;                             // 评分
    private Integer consultationCount;                  // 咨询次数
    private BigDecimal price;                               // 价格
}
