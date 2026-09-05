package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 库存异动流水实体类
 * @Datetime: 2026-02-28
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "库存异动流水实体")
public class InventoryTransaction {
    @Schema(description = "异动ID")
    @TableId
    private Long id;

    @Schema(description = "药品ID")
    private Long drugId;

    @Schema(description = "仓库ID")
    private Long warehouseId;

    @Schema(description = "异动类型(1:入库,2:出库,3:盘点调整,4:锁定,5:解锁)")
    private Integer transactionType;

    @Schema(description = "关联单号(采购单/销售单等)")
    private String relatedOrder;

    @Schema(description = "数量变化(正数表示增加，负数表示减少)")
    private Integer quantityChange;

    @Schema(description = "变化前数量")
    private Integer quantityBefore;

    @Schema(description = "变化后数量")
    private Integer quantityAfter;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
