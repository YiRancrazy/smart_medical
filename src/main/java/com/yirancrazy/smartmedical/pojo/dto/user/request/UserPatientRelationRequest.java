package com.yirancrazy.smartmedical.pojo.dto.user.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加就诊人请求
 * @Author: YiRanCrazy@gmail.com
 * @Description: 添加就诊人请求参数
 * @Datetime: 2026-07-18 21:20
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPatientRelationRequest {

    private String name;

    private String idCard;

    private String phone;

    private String relation;

    private String remark;

    private String defaulted;
}
