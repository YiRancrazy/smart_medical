package com.yirancrazy.smartmedical.pojo.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-08 11:06
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class  PhoneAndPasswordLoginRequest {
    private String phone;       // 手机号
    private String password;    // 密码
}