package com.yirancrazy.smartmedical.pojo.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-20 08:29
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "医生Vo")
public class DoctorVo {
    @Schema(description = "医生id")
    private String doctorId;                       // 医生id

    @Schema(description = "科室id")
    private Long departmentId;                   // 科室id

    @Schema(description = "科室名称")
    private String departmentName;                 // 科室名称

    @Schema(description = "姓名")
    private String doctorName;                     // 姓名

    @Schema(description = "头像")
    private String avatar;                         // 头像

    @Schema(description = "医生职位id")
    private String doctorPositionId;               // 医生职位id

    @Schema(description = "医生职位名称")
    private String positionName;                   // 医生职位名称

    @Schema(description = "学历")
    private String degreeId;                       // 学历

    @Schema(description = "学历名称")
    private String degreeName;                     // 学历名称

    @Schema(description = "家庭住址")
    private String address;                        // 家庭住址

    @Schema(description = "评分")
    private Double scope;                          // 评分

    @Schema(description = "标签")
    private String tags;                           // 标签

    @Schema(description = "描述")
    private String description;                    // 描述

    @Schema(description = "状态，0、在职、1、休假、2、出差、3、离职")
    private Integer status;                        // 状态，0、在职、1、休假、2、出差、3、离职
}
