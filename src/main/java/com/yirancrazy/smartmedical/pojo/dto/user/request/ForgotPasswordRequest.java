package com.yirancrazy.smartmedical.pojo.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 忘记密码重置请求（用户端）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 手机号 + 短信验证码 + 新密码完成未登录改密，验证码防止任意账号被接管
 * @Datetime: 2026-09-05 12:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;       // 手机号

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码格式不正确")
    private String code;        // 短信验证码

    @NotBlank(message = "新密码不能为空")
    private String password;    // 新密码
}