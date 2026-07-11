package com.yirancrazy.smartmedical.pojo.vo.registration.confirm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springdoc.webmvc.core.fn.SpringdocRouteBuilder;

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
    private String userId;  // 就诊人id
    private String patientCardNo;  // 就诊人卡号
    private String patientUserId;  // 就诊人卡用户id
    private String patientName;  // 就诊人名称
    private String patientAvatar;  // 就诊人卡头像
    private String patientIdCard;  // 就诊人卡身份证号
    private String patientPhone; // 就诊人卡手机号
    private String relation;  // 就诊人卡关系
    private Boolean defaultPatientCard;   // 就诊人卡是否默认
}
