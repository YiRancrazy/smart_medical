package com.yirancrazy.smartmedical.pojo.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送短信验证码请求（用户注册）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户输入手机号请求发送注册验证码
 * @Datetime: 2026-09-04 20:30
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsCodeRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;       // 手机号
}
