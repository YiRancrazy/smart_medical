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
import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号价格实体类
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "挂号价格实体")
public class RegistrationPrice {

    @Schema(description = "价格ID")
    @TableId
    private Long id;                          // 价格ID

    @Schema(description = "职位ID（外键，关联doctor_position表）")
    private Long positionId;                       // 职位ID

    @Schema(description = "价格类型（1:普通门诊 2:急诊 3:节假日）")
    private Integer priceType;                         // 价格类型

    @Schema(description = "挂号价格（元）")
    private BigDecimal price;                          // 挂号价格

    @Schema(description = "价格描述（如：主任医师普通门诊）")
    private String description;                        // 价格描述

    @Schema(description = "创建时间")
    private LocalDateTime createTime;                  // 创建时间

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;                  // 更新时间

    @Schema(description = "是否删除（0:正常 1:删除）")
    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean deleted;
}
