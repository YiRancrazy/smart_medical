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
 * @Description: 诊室状态实体类
 * @Datetime: 2026-03-06
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "诊室状态实体")
public class ConsultingRoomStatus {
    @Schema(description = "ID")
    @TableId
    private Long id;

    @Schema(description = "诊室ID")
    private Long consultingRoomId;

    @Schema(description = "占用开始时间")
    private LocalDateTime occupancyStartTime;

    @Schema(description = "占用结束时间")
    private LocalDateTime occupancyEndTime;

    @Schema(description = "占用原因（如：设备维护、会议、午休等）")
    private String occupancyReason;

    @Schema(description = "状态: 1-可用, 0-停用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
