package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 排班实体类
 * @Datetime: 2026-02-02 13:04
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "排班实体")
public class RegistrationSchedule {
    @Schema(description = "排班ID")
    @TableId
    private Long id;

    @Schema(description = "关联医生ID")
    private Long doctorId;

    @Schema(description = "生成模板id")
    @TableField("registration_schedule_template_id")
    private Long registrationScheduleTemplateId;

    @Schema(description = "排班开始时间")
    private LocalDateTime startTime;

    @Schema(description = "排班结束时间")
    private LocalDateTime endTime;

    @Schema(description = "状态: 1-正常, 0-停诊, 2-已满")
    private Integer status;

    @Schema(description = "剩余号源数")
    private Integer remainingQuota;


    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
