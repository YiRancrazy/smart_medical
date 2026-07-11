package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品库存实体类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "药品库存实体")
public class DrugInventory {
    @Schema(description = "库存记录ID")
    @TableId
    private Long id;

    @Schema(description = "药品ID")
    private Long drugId;

    @Schema(description = "仓库ID")
    private Long warehouseId;

    @Schema(description = "库存数量")
    private Integer stockQuantity;

    @Schema(description = "可用数量")
    private Integer availableQuantity;

    @Schema(description = "锁定数量")
    private Integer lockedQuantity;

    @Schema(description = "最低库存预警线")
    private Integer minStock;

    @Schema(description = "最高库存")
    private Integer maxStock;

    @Schema(description = "最后入库时间")
    private LocalDateTime lastInboundTime;

    @Schema(description = "最后出库时间")
    private LocalDateTime lastOutboundTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
