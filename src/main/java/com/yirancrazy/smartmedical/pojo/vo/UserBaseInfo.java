package com.yirancrazy.smartmedical.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-10 13:36
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBaseInfo {
    private String accountId;       // 账号id
    private String userId;          // 用户id
    private String nickname;        // 昵称
    private String username;        // 用户名
    private String avatar;          // 头像
}