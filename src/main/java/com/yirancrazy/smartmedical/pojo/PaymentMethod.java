package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支付方式实体类
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付方式实体")
public class PaymentMethod {
    @Schema(description = "支付方式id")
    @TableId
    private Long id;

    @Schema(description = "支付方式名称")
    private String name;

    @Schema(description = "父级支付类型")
    private Long parentId;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "是否是默认支付方式")
    @TableField(value = "is_default")
    private Boolean defaulted;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "是否启用")
    @TableField(value = "is_enabled")
    private Boolean enabled;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean deleted;
}
