package com.yirancrazy.smartmedical.pojo.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户手机号换绑请求
 * @Author: YiRanCrazy@gmail.com
 * @Description: 旧手机号验证码、新手机号及新手机号验证码
 * @Datetime: 2026-09-15 00:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户手机号换绑请求")
public class UpdateUserPhoneRequest {

    @Schema(description = "旧手机号验证码")
    @NotBlank(message = "旧手机号验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "旧手机号验证码格式不正确")
    private String oldPhoneCode;

    @Schema(description = "新手机号")
    @NotBlank(message = "新手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "新手机号格式不正确")
    private String newPhone;

    @Schema(description = "新手机号验证码")
    @NotBlank(message = "新手机号验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "新手机号验证码格式不正确")
    private String newPhoneCode;
}
