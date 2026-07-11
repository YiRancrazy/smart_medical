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
 * @Description: 科室实体类
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "科室实体")
public class Department {

    @Schema(description = "科室id")
    @TableId
    private Long id;                              // 科室id

    @Schema(description = "科室编号（对外公开）")
    private Long sn;                              // 科室编号（对外公开）

    @Schema(description = "科室名称")
    private String name;                          // 科室名称

    @Schema(description = "科室类别(0-临床，1-医技，2-行政)")
    private String type;                          // 科室类别(0-临床，1-医技，2-行政)

    @Schema(description = "上级科室ID")
    private Long parentDepartmentId;              // 上级科室ID

    @Schema(description = "部门负责人id")
    private Long manager;                         // 部门负责人id

    @Schema(description = "科室电话")
    private String phone;                         // 科室电话

    @Schema(description = "科室地址")
    private String address;                       // 科室地址

    @Schema(description = "状态(1-启用，0-停用)")
    private Integer status;                       // 状态(1-启用，0-停用)

    @Schema(description = "描述")
    private String description;                   // 描述

    @Schema(description = "记录创建时间")
    private LocalDateTime createTime;             // 记录创建时间

    @Schema(description = "记录更新时间")
    private LocalDateTime updateTime;             // 记录更新时间

    @Schema(description = "删除（1是0否）")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
