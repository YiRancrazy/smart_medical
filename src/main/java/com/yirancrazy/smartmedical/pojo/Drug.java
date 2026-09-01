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
 * @Description: 药品信息实体类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "药品信息实体")
public class Drug {
    @Schema(description = "药品ID")
    @TableId
    private Long id;

    @Schema(description = "药品编码")
    private String drugCode;

    @Schema(description = "通用名")
    private String commonName;

    @Schema(description = "商品名")
    private String tradeName;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "规格")
    private String specification;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "生产厂家")
    private String manufacturer;

    @Schema(description = "批准文号")
    private String approvalNumber;

    @Schema(description = "剂型")
    private String dosageForm;

    @Schema(description = "是否处方药")
    private Integer isPrescription;

    @Schema(description = "是否麻醉药品")
    private Integer isNarcotic;

    @Schema(description = "是否精神药品")
    private Integer isPsychotropic;

    @Schema(description = "是否抗生素")
    private Integer isAntibiotic;

    @Schema(description = "使用方法")
    private String usageMethod;

    @Schema(description = "副作用")
    private String sideEffects;

    @Schema(description = "禁忌")
    private String contraindications;

    @Schema(description = "单价")
    private Integer price;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean deleted;
}
