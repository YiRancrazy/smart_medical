package com.yirancrazy.smartmedical.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


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
    private String patientId;             // 患者ID
    private String patientCardId;         // 就诊卡ID
    private String patientCardSn;         // 就诊卡编号
    private String patientName;           // 患者姓名
    private String patientAvatar;         // 患者头像（患者模型暂不存储头像）
    private String patientIdCard;         // 患者身份证号
    private String patientPhone;          // 患者手机号
    private Integer patientSex;           // 患者性别（0男，1女）
    private String outPatientBalance;     // 门诊账户余额
}
