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
 * @Description: 仓库实体类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "仓库实体")
public class Warehouse {
    @Schema(description = "仓库ID")
    @TableId
    private Long id;

    @Schema(description = "仓库编码")
    private String code;

    @Schema(description = "仓库名称")
    private String name;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "负责人")
    private String manager;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "是否默认仓库")
    private Integer isDefault;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
