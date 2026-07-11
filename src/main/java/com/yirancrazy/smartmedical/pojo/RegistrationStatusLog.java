package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 挂号状态变更日志实体类
 * @Author: YiRanCrazy@gmail.com
 * @Description: 每次状态迁移落库，便于追溯
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "挂号状态变更日志实体")
public class RegistrationStatusLog {
    @Schema(description = "日志ID")
    @TableId
    private Long id;

    @Schema(description = "挂号ID")
    private Long registrationId;

    @Schema(description = "变更前状态")
    private Integer fromStatus;

    @Schema(description = "变更后状态")
    private Integer toStatus;

    @Schema(description = "操作人ID (0=系统)")
    private Long operatorId;

    @Schema(description = "操作人角色(user/doctor/pharmacist/system)")
    private String operatorRole;

    @Schema(description = "变更备注/原因")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}