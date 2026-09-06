package com.yirancrazy.smartmedical.pojo.dto.admin.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理员端 - 编辑医生信息请求
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员端编辑医生信息请求 DTO，null 字段不更新
 * @Datetime: 2026-09-06 11:08
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDoctorUpdateRequest {

    @NotBlank(message = "医生姓名不能为空")
    private String name;                    // 姓名

    private Long departmentId;              // 科室id

    private Long positionId;                // 职称id（doctor_position）

    private Long degreeId;                  // 学历id

    private String avatar;                  // 头像

    private String address;                 // 家庭住址

    private Double scope;                   // 评分

    private List<String> tags;              // 标签，后端拼逗号入库

    private String description;             // 描述

    private Integer status;                 // 状态，0、在职、1、休假、2、出差、3、离职
}
