package com.yirancrazy.smartmedical.pojo.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-05 19:33
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponseSimple {
    private String id;              // 管理员id
    private String username; // 管理员用户名
    private String nickname; // 管理员昵称
    private String avatar; // 管理员头像
    private String phone; // 管理员手机号
    private String email; // 管理员邮箱
    private String role; // 管理员角色
}