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
 * @Description: 文件实体类
 * @Datetime: 2026-03-06
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文件实体")
public class File {
    @Schema(description = "文件id")
    @TableId
    private Long id;

    @Schema(description = "上传管理员id")
    private Long adminId;

    @Schema(description = "文件名")
    private String name;

    @Schema(description = "文件md5值")
    private String md5;

    @Schema(description = "文件位置")
    private String path;

    @Schema(description = "文件大小")
    private Long size;

    @Schema(description = "是否启用")
    private Boolean enable;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
