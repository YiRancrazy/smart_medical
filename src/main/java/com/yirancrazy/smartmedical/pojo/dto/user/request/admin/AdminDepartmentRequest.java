package com.yirancrazy.smartmedical.pojo.dto.user.request.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-07 10:33
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDepartmentRequest {
    private String id;                        // 科室ID
    private String sn;                        // 科室编号
    private String name;                      // 科室名称
    private String type;                      // 科室类型
    private String patentDepartmentId;        // 上级科室ID
    private String managerId;                 // 科室管理员ID
    private String phone;                     // 科室电话
    private String address;                   // 科室地址
    private String status;                    // 科室状态
    private String description;               // 科室描述
}