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
 * @Description: 挂号记录实体类
 * @Datetime: 2026-02-02 13:04
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "挂号记录实体")
public class Registration {
    @Schema(description = "挂号记录ID")
    @TableId
    private Long id;

    @Schema(description = "关联用户ID")
    private Long userId;

    @Schema(description = "关联挂号模板id")
    private Long registrationScheduleTemplateId;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "状态: 1-待就诊, 2-已就诊, 3-已取消, 4-已退号")
    private Integer status;

    @Schema(description = "挂号时间")
    private LocalDateTime registrationTime;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
