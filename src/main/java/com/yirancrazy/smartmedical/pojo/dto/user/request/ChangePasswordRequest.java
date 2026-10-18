package com.yirancrazy.smartmedical.pojo.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 修改密码请求（登录态使用）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 登录态修改登录密码，需校验旧密码
 * @Datetime: 2026-09-04 16:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "原密码不能为空")
    private String oldPassword;  // 原密码

    @NotBlank(message = "新密码不能为空")
    private String newPassword;  // 新密码
}