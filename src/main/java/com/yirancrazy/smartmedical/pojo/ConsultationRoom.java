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
 * @Description: 诊室实体类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "诊室实体")
public class ConsultationRoom {
    @Schema(description = "诊室ID")
    @TableId
    private Long id;

    @Schema(description = "诊室编号")
    private Long sn;

    @Schema(description = "诊室名称")
    private String name;

    @Schema(description = "所属科室")
    private Long departmentId;

    @Schema(description = "具体位置")
    private String location;

    @Schema(description = "设备描述")
    private String equipmentDesc;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean deleted;
}
