package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 处方明细实体类
 * @Author: YiRanCrazy@gmail.com
 * @Description: 一行 = 一个药品，含快照单价
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "处方明细实体")
public class PrescriptionItem {
    @Schema(description = "处方明细ID")
    @TableId
    private Long id;

    @Schema(description = "处方ID")
    private Long prescriptionId;

    @Schema(description = "药品ID")
    private Long drugId;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "快照单价(分)")
    private Integer unitPrice;

    @Schema(description = "使用方法")
    private String usageMethod;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}