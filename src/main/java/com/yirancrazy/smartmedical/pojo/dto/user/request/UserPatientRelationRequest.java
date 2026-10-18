package com.yirancrazy.smartmedical.pojo.dto.user.request;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "身份证号不能为空")
    private String idCard;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "关系不能为空")
    private String relation;

    private String remark;

    private String defaulted;
}
