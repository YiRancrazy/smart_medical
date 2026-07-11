package com.yirancrazy.smartmedical.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户患者关系实体类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户患者关系实体")
@TableName("user_patient_relation")
public class UserPatientRelation {

    @Schema(description = "就诊人关系Id")
    @TableId
    private Long id;

    @Schema(description = "用户id")
    private Long userId;

    @Schema(description = "就诊人id")
    private Long patientUserId;

    @Schema(description = "备注标题")
    private String remark;

    @Schema(description = "是否授权（0-否，1-是）")
    private Integer isAuthorized;

    @Schema(description = "是否是默认就诊人")
    @TableField(value = "is_default")
    private Boolean defaulted;

    @Schema(description = "与就诊人关系")
    private String relation;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
