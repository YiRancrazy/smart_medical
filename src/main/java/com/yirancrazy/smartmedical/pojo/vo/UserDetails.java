package com.yirancrazy.smartmedical.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-04 18:21
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetails {
    private String accountId;          // 账号ID
    private String userId;             // 用户ID
    private String email;              // 邮箱
    private String phone;              // 手机号
    private String avatar;             // 头像
    private Integer sex;               // 性别
    private String nickName;           // 昵称
    private String username;           // 用户真实姓名
}