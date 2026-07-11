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
 * @Description: 订单明细实体类
 * @Datetime: 2026-02-02 13:04
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单明细实体")
public class OrderItem {
    @Schema(description = "订单明细项id")
    @TableId
    private Long id;

    @Schema(description = "订单id")
    private Long orderId;

    @Schema(description = "项目明细ID")
    private Long productionId;

    @Schema(description = "项目类型ID")
    private Long productionTypeId;

    @Schema(description = "购买数量")
    private Integer quantity;

    @Schema(description = "项目明细名称")
    private String productionName;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
