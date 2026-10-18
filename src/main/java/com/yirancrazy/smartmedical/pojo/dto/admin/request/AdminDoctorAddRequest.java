package com.yirancrazy.smartmedical.pojo.dto.admin.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理员端 - 添加医生请求
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员端添加医生请求 DTO，同时用于创建医生登录账户
 * @Datetime: 2026-09-06 14:32
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDoctorAddRequest {

    @NotBlank(message = "医生姓名不能为空")
    private String name;                    // 姓名

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;                   // 手机号（登录账户）

    @NotNull(message = "科室不能为空")
    private Long departmentId;              // 科室id

    @NotNull(message = "职称不能为空")
    private Long positionId;                // 职称id（doctor_position）

    @NotNull(message = "学历不能为空")
    private Long degreeId;                  // 学历id

    @Email(message = "邮箱格式不正确")
    private String email;                   // 邮箱（可选）

    private String avatar;                  // 头像 URL（可选）

    private String address;                 // 家庭住址（可选）

    private List<String> tags;              // 标签，后端拼逗号入库（可选）

    private String description;             // 描述（可选）
}
