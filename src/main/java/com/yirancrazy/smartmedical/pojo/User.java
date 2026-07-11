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
 * @Description: 用户实体类
 * @Datetime: 2026-02-02 13:04
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户实体")
public class User {
    @Schema(description = "用户id")
    @TableId
    private Long id;

    @Schema(description = "用户姓名")
    private String nickname;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "家庭住址")
    private String address;

    @Schema(description = "身份证")
    private String idCard;

    @Schema(description = "性别（0男，1女)")
    private Integer sex;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
