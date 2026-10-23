package com.yirancrazy.smartmedical.pojo.vo.registration.confirm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号确认页面就诊人信息返回对象
 * @Datetime: 2026-02-20 09:51
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationConfirmPatientCardVo {
    private String userId;  // 当前登录账号id（兼容字段，非患者ID）
    private String patientCardNo;  // 就诊人卡号
    private String patientUserId;  // 患者本人绑定账号id（兼容字段，非患者ID）
    private String patientId;  // 患者ID
    private String patientName;  // 患者姓名
    private String patientAvatar;  // 患者头像（患者模型暂不存储头像）
    private String patientIdCard;  // 患者身份证号
    private String patientPhone; // 患者手机号
    private Integer patientSex; // 患者性别（0男，1女）
    private String relation;  // 与患者关系
    private Boolean defaultPatientCard;   // 是否默认就诊人
}
