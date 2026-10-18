package com.yirancrazy.smartmedical.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 本人就诊卡状态
 * @Author: YiRanCrazy@gmail.com
 * @Description: 当前用户的本人就诊卡完善状态与回填信息
 * @Datetime: 2026-09-14 00:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "本人就诊卡状态")
public class SelfPatientCardStatus {

    @Schema(description = "本人就诊卡信息是否已完善")
    private Boolean completed;

    @Schema(description = "本人就诊人关系ID")
    private String relationId;

    @Schema(description = "姓名")
    private String patientName;

    @Schema(description = "身份证号")
    private String patientIdCard;

    @Schema(description = "手机号")
    private String patientPhone;

    @Schema(description = "就诊卡号")
    private String patientCardSn;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "是否为默认就诊人")
    private Boolean defaultPatient;
}
