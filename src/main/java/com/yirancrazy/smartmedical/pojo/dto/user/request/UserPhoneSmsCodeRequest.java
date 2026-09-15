package com.yirancrazy.smartmedical.pojo.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户手机号换绑验证码请求
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户获取旧手机号或新手机号换绑验证码
 * @Datetime: 2026-09-15 00:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户手机号换绑验证码请求")
public class UserPhoneSmsCodeRequest {

    @Schema(description = "手机号")
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "验证码场景（old旧手机号，new新手机号）")
    @NotBlank(message = "验证码场景不能为空")
    @Pattern(regexp = "^(old|new)$", message = "验证码场景不正确")
    private String scene;
}
