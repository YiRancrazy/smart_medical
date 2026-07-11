package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班模板实体类
 * @Datetime: 2026-02-26
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "挂号排班模板实体")
public class RegistrationScheduleTemplate {
    @Schema(description = "挂号排班模板id")
    @TableId
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "关联医生id")
    private Long doctorId;

    @Schema(description = "挂号日期")
    private LocalDate registrationDate;

    @Schema(description = "挂号类型（0普通号 1、专家号 2、 外院专家号）")
    private Integer registrationType;

    @Schema(description = "状态(0 正常，1停诊）")
    private Integer status;
    @Schema(description = "开始时间")
    private LocalTime startTime;

    @Schema(description = "结束时间")
    private LocalTime endTime;

    @Schema(description = "总号源数")
    private Integer totalQuota;

    @Schema(description = "挂号价格")
    private Integer price;

    @Schema(description = "优先级(0、普通;1、特殊;2、紧急)")
    private Integer priority;

    @Schema(description = "是否启用")
    @TableField(value = "is_enabled")
    private Boolean enabled;

    @Schema(description = "就诊地址id")
    private Long consultationRoomId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
