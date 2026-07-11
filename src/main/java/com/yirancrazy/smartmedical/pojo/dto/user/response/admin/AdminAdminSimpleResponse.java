package com.yirancrazy.smartmedical.pojo.dto.user.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-06 21:26
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAdminSimpleResponse {
    private String id;       // 管理员id
    private String username; // 管理员用户名
    private String phone; // 管理员手机号
    private String avatar; // 管理员头像
    private String remark; // 管理员备注
    private String departmentId; // 管理员所属部门id
    private String departmentName;
    private String role; // 管理员角色
    private String roleId; // 管理员角色id
    private String status; // 管理员状态
    private String email;  // 管理员邮箱
}