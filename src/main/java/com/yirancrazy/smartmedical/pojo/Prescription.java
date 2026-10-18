package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 处方实体类
 * @Author: YiRanCrazy@gmail.com
 * @Description: 处方头，含乐观锁 version 字段
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "处方实体")
public class Prescription {
    @Schema(description = "处方ID")
    @TableId
    private Long id;

    @Schema(description = "病历ID")
    private Long medicalRecordId;

    @Schema(description = "关联药品订单ID")
    private Long orderId;

    @Schema(description = "处方金额(分)")
    private Integer totalAmount;

    @Schema(description = "状态: 0-待支付 1-已支付 2-已发药 3-已取消")
    private Integer status;

    @Schema(description = "发药药师ID")
    private Long pharmacistId;

    @Schema(description = "发药时间")
    private LocalDateTime dispensedAt;

    @Schema(description = "乐观锁版本号")
    @Version
    private Integer version;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean deleted;
}