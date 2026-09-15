package com.yirancrazy.smartmedical.pojo.dto.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户个人信息请求
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户补充姓名、昵称、身份证号、性别和家庭住址
 * @Datetime: 2026-09-14 00:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新用户个人信息请求")
public class UpdateUserProfileRequest {

    @Schema(description = "姓名")
    @NotBlank(message = "姓名不能为空")
    @Size(max = 64, message = "姓名长度不能超过64个字符")
    private String username;

    @Schema(description = "昵称")
    @Size(max = 64, message = "昵称长度不能超过64个字符")
    private String nickname;

    @Schema(description = "身份证号")
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)", message = "身份证号格式不正确")
    private String idCard;

    @Schema(description = "性别（0男，1女）")
    @NotNull(message = "性别不能为空")
    @Min(value = 0, message = "性别参数不正确")
    @Max(value = 1, message = "性别参数不正确")
    private Integer sex;

    @Schema(description = "家庭住址")
    @Size(max = 255, message = "家庭住址长度不能超过255个字符")
    private String address;
}
