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
 * @Description: 医生实体类
 * @Datetime: 2026-02-02 13:04
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "医生实体")
public class Doctor {
    @Schema(description = "医生id")
    @TableId
    private Long id;                              // 医生id

    @Schema(description = "科室id")
    private Long departmentId;                    // 科室id

    @Schema(description = "姓名")
    private String name;                          // 姓名

    @Schema(description = "头像")
    private String avatar;                        // 头像

    @Schema(description = "医生职位id")
    private Long doctorPositionId;                // 医生职位id

    @Schema(description = "学历")
    private Long degreeId;                        // 学历

    @Schema(description = "家庭住址")
    private String address;                       // 家庭住址

    @Schema(description = "评分")
    private Double scope;                         // 评分

    @Schema(description = "标签")
    private String tags;                          // 标签

    @Schema(description = "描述")
    private String description;                   // 描述

    @Schema(description = "状态，0、在职、1、休假、2、出差、3、离职")
    private Integer status;                       // 状态，0、在职、1、休假、2、出差、3、离职

    @Schema(description = "创建日期")
    private LocalDateTime createTime;             // 创建日期

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;             // 更新日期

    @Schema(description = "是否删除")
    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean deleted;
}
