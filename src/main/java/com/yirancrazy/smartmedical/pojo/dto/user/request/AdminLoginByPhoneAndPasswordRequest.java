package com.yirancrazy.smartmedical.pojo.dto.user.request;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-05 18:27
 * @Version: 1.0
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminLoginByPhoneAndPasswordRequest {
    private String phone;  // 手机号
    private String password; // 密码
    private Boolean remember; // 记住
}