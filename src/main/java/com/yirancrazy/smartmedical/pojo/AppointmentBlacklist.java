package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 预约黑名单实体类
 * @Datetime: 2026-02-28
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "预约黑名单实体")
public class AppointmentBlacklist {
    @Schema(description = "ID")
    @TableId
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "就诊人ID")
    private Long patientId;

    @Schema(description = "类型(1爽约2频繁取消3其他)")
    private Integer blacklistType;

    @Schema(description = "加入黑名单原因")
    private String reason;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "是否生效")
    private Boolean isActive;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
