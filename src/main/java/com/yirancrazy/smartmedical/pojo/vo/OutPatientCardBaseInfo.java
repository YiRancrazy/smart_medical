package com.yirancrazy.smartmedical.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 门诊患者基本信息
 * @Datetime: 2026-02-13 15:35
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutPatientCardBaseInfo {
    private String patientId;                        // 就诊人id
    private String patientCardId;                       // 就诊人卡id
    private String patientCardSn;                         // 就诊人编号
    private String patientName;                       // 就诊人姓名
    private String patientAvatar;                     // 就诊人头像
    private String patientIdCard;                     // 就诊人身份证号
    private String outPatientBalance;                       // 门诊账户余额
}
