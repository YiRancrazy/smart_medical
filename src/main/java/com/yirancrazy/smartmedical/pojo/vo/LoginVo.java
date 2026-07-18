package com.yirancrazy.smartmedical.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 返回登录信息
 * @Datetime: 2026-02-03 11:50
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVo {
    private String accountId;
    private String token;
    private String uid;
    private String phone;
    private String userName;
}
