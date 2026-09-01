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
 * @Description: 账号实体类
 * @Datetime: 2026-02-02 13:04
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账号实体")
public class Account {
    @Schema(description = "账户id")
    @TableId
    private Long id;                              // 账户id

    @Schema(description = "用户id")
    private Long userId;                           // 用户id

    @Schema(description = "用户角色")
    private Long roleId;                           // 用户角色

    @Schema(description = "密码")
    private String password;                       // 密码

    @Schema(description = "邮箱")
    private String email;                          // 邮箱

    @Schema(description = "手机号")
    private String phone;                          // 手机号

    @Schema(description = "是否启用")
    @TableField(value = "is_enabled")
    private Boolean enabled;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;              // 创建日期

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;              // 更新日期

    @Schema(description = "是否删除")
    @TableLogic
    @TableField(value = "is_deleted", fill = FieldFill.INSERT)
    private Boolean deleted;
}
