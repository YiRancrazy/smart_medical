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
 * @Description: 医生职位实体类
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "医生职位实体")
public class DoctorPosition {
    @Schema(description = "职位id")
    @TableId
    private Long id;                              // 职位id

    @Schema(description = "职位名称")
    private String name;                          // 职位名称

    @Schema(description = "职位描述")
    private String description;                    // 职位描述

    @Schema(description = "创建日期")
    private LocalDateTime createTime;              // 创建日期

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;              // 更新日期

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
