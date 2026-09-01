package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 药品实体
 * <p>实际表为 drug（DDL CreateTable.sql），此处用 @TableName 指向 drug 表。
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药品实体
 * @Datetime: 2026-02-02 13:04
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "药品实体")
@TableName("drug")
public class Medicine {

    @Schema(description = "药品id")
    @TableId(value = "id", type = IdType.AUTO)
    private Long medicineId;

    @Schema(description = "国药准字")
    @TableField("approval_number")
    private String approvalNumber;

    @Schema(description = "药品名称(通用名)")
    @TableField("common_name")
    private String medicineName;

    @Schema(description = "药品剂型")
    @TableField("dosage_form")
    private String dosageForm;

    @Schema(description = "药品规格")
    @TableField("specification")
    private String medicineSpecifications;

    @Schema(description = "制药公司")
    @TableField("manufacturer")
    private String listingPermitHolder;

    /** drug 表无此字段，标注 exist=false 不参与 SQL */
    @Schema(description = "生产公司")
    @TableField(exist = false)
    private String productionUnit;

    @Schema(description = "药品编号")
    @TableField("drug_code")
    private String medicineCode;

    /** drug 表无此字段 */
    @Schema(description = "备注")
    @TableField(exist = false)
    private String remarks;

    @Schema(description = "创建时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = com.baomidou.mybatisplus.annotation.FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** drug 表无此字段 */
    @Schema(description = "药品数量")
    @TableField(exist = false)
    private Integer quantity;

    @Schema(description = "价格（单位：分）")
    @TableField("price")
    private Integer price;
}
