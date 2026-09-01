package com.yirancrazy.smartmedical.pojo.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-05 18:27
 * @Version: 1.0
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminLoginByPhoneAndPasswordRequest {
    @NotBlank(message = "手机号不能为空")
    private String phone;  // 手机号

    @NotBlank(message = "密码不能为空")
    private String password; // 密码

    private Boolean remember; // 记住
}