package com.yirancrazy.smartmedical.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户个人信息
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端个人信息与完善状态
 * @Datetime: 2026-09-14 00:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户个人信息")
public class UserProfileInfo {

    @Schema(description = "用户ID")
    private String userId;

    @Schema(description = "姓名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "性别（0男，1女）")
    private Integer sex;

    @Schema(description = "家庭住址")
    private String address;

    @Schema(description = "个人信息是否已完善")
    private Boolean profileCompleted;
}
