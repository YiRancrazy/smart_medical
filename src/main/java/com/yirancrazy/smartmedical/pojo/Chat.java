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
 * @Description: 聊天实体类
 * @Datetime: 2026-02-02 13:04
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天实体")
public class Chat {
    @Schema(description = "聊天id")
    @TableId
    private Long id;                           // 聊天id

    @Schema(description = "发送id")
    private Long sendId;                           // 发送id

    @Schema(description = "接收id")
    private Long receiveId;

    @Schema(description = "聊天类型")
    private Integer type;

    @Schema(description = "0文字，1图片，2、视频")
    private Integer contentType;                      // 0文字，1图片，2、视频

    @Schema(description = "内容")
    private String content;                    // 内容

    @Schema(description = "创建日期")
    private LocalDateTime createTime;              // 创建日期

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;              // 更新日期

    @Schema(description = "是否删除")
    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean deleted;                     // 是否删除
}
