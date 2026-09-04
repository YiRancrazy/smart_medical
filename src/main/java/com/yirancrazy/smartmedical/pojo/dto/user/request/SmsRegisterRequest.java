package com.yirancrazy.smartmedical.pojo.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信验证码注册请求（用户注册）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 手机号 + 验证码完成用户注册
 * @Datetime: 2026-09-04 20:30
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsRegisterRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;       // 手机号

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String code;        // 短信验证码
}
