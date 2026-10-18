package com.yirancrazy.smartmedical.pojo.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 就诊卡简单响应对象
 * @Datetime: 2026-03-01
 * @Version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientCardSimpleResponse {
    private String userId;              // 用户id
    private String userPatientRelationId; // 用户患者关系id
    private String patientId;           // 就诊人ID
    private String patientName;         // 就诊人姓名
    private String patientIdCard;       // 身份证号
    private String patientPhone;        // 手机号码
    private String patientCardSn;       // 就诊卡编号
    private String patientCardId;       // 就诊卡ID
    private String relation;            // 与本人关系
    private Boolean defaultPatient; // 是否默认就诊人
    private String remark;              // 备注（U06: 编辑页需回填，原列表接口丢失此字段）
}
