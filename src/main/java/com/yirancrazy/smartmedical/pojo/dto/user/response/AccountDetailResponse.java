package com.yirancrazy.smartmedical.pojo.dto.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 账户详情响应对象
 * @Datetime: 2026-03-06
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账户详情响应")
public class AccountDetailResponse {

    @Schema(description = "账户ID")
    private Long accountId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "角色Id")
    private Long roleId;

    @Schema(description = "科室名称")
    private String department;

    @Schema(description = "科室ID")
    private Long departmentId;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
