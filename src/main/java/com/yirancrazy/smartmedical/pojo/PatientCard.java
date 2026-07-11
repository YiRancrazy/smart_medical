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
 * @Description: 就诊卡实体类
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "就诊卡实体")
public class PatientCard {
    @Schema(description = "就诊卡id")
    @TableId
    private Long id;

    @Schema(description = "就诊卡卡号")
    private Long sn;

    @Schema(description = "住院余额")
    private Integer inpatientBalance;

    @Schema(description = "门诊余额")
    private Integer outpatientBalance;

    @Schema(description = "支付密码（加密存储）")
    private String paymentPassword;

    @Schema(description = "就诊卡状态，0-正常，1-冻结 2-注销")
    private Integer status;

    @Schema(description = "二维码")
    private String qrCode;

    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    @Schema(description = "更新日期")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableField(value = "is_deleted")
    private Boolean deleted;
}
