package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 预约规则配置实体类
 * @Datetime: 2026-02-28
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "预约规则配置实体")
public class AppointmentRule {
    @Schema(description = "规则ID")
    @TableId
    private Long id;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则类型(1普通2专家3急诊)")
    private Integer ruleType;

    @Schema(description = "适用科室ID(NULL表示全院通用)")
    private Long departmentId;

    @Schema(description = "适用医生ID(NULL表示科室通用)")
    private Long doctorId;

    @Schema(description = "可提前预约天数")
    private Integer advanceDays;

    @Schema(description = "开始预约时间(如08:00)")
    private LocalTime appointmentStartTime;

    @Schema(description = "结束预约时间(如17:00)")
    private LocalTime appointmentEndTime;

    @Schema(description = "最少提前小时数")
    private Integer minAdvanceHours;

    @Schema(description = "最大提前天数")
    private Integer maxAdvanceDays;

    @Schema(description = "每日号源总数")
    private Integer dailyQuota;

    @Schema(description = "每时间段号源数")
    private Integer perTimeQuota;

    @Schema(description = "时间间隔(分钟)")
    private Integer timeInterval;

    @Schema(description = "可取消预约提前小时数")
    private Integer cancelBeforeHours;

    @Schema(description = "每日取消次数限制")
    private Integer cancelLimitPerDay;

    @Schema(description = "爽约次数限制")
    private Integer noShowLimit;

    @Schema(description = "爽约锁定天数")
    private Integer noShowLockDays;

    @Schema(description = "节假日号源比例")
    private BigDecimal holidayQuotaRatio;

    @Schema(description = "是否开启节假日预约")
    private Boolean enableHolidayAppointment;

    @Schema(description = "状态(0停用1启用)")
    private Integer status;

    @Schema(description = "生效日期")
    private LocalDate effectiveDate;

    @Schema(description = "失效日期")
    private LocalDate expiryDate;

    @Schema(description = "优先级(0:医生 1、科室、2、医院）")
    private Integer priority;

    @Schema(description = "规则描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean deleted;
}
