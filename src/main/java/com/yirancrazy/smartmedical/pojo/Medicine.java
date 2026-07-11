package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品实体类
 * @Datetime: 2026-02-02 13:04
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "药品实体")
public class Medicine {
    @Schema(description = "药品id")
    @TableId
    private Long medicineId;                       // 药品id
    @Schema(description = "国药准字")
    private String approvalNumber;                 // 国药准字
    @Schema(description = "药品名称")
    private String medicineName;                   // 药品名称
    @Schema(description = "药品剂型")
    private String dosageForm;                     // 药品剂型
    @Schema(description = "药品剂量")
    private String medicineSpecifications;         // 药品剂量
    @Schema(description = "制药公司")
    private String listingPermitHolder;            // 制药公司
    @Schema(description = "生产公司")
    private String productionUnit;                 // 生产公司
    @Schema(description = "药品编号")
    private String medicineCode;                   // 药品编号
    @Schema(description = "备注")
    private String remarks;                        // 备注
    @Schema(description = "创建时间")
    private LocalDateTime createTime;              // 创建时间
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;              // 更新时间
    @Schema(description = "药品数量")
    private Integer quantity;                      // 药品数量
    @Schema(description = "价格")
    private BigDecimal price;                      // 价格
}
