package com.yirancrazy.smartmedical.pojo.dto.user.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-06 20:08
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDepartmentSimpleResponse {
    private String id;                             // 科室ID
    private String sn; //科室编号
    private String name;                           // 科室名称
    private String type;                           // 科室类型
    private String parentDepartmentId; //上级科室ID
    private String parentDepartmentName; //上级科室名称
    private String description;                       // 科室描述
    private String managerId;                           // 科室管理员ID
    private String managerName;                         // 科室管理员名称
    private String managerPhone;                       // 科室管理员手机号
    private String phone;                               // 科室电话
    private String address;                               // 科室地址
    private String status;                               // 科室状态
}