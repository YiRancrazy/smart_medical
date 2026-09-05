package com.yirancrazy.smartmedical.controller.doctor;

import com.yirancrazy.smartmedical.manager.AdminAuthManager;
import com.yirancrazy.smartmedical.manager.AuthManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.request.AdminLoginByPhoneAndPasswordRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminResponseSimple;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生端登录控制器
 * @Datetime: 2026-07-17 11:30
 * @Version: 1.0
 */

@RestController
@RequestMapping("/api/doctor/v1/auth")
@Tag(name = "医生端-登录控制器")
@RequiredArgsConstructor
public class DoctorAuthControllerV1 {

    /** 医生角色 id（来自 role 表） */
    private static final Long DOCTOR_ROLE_ID = 2L;

    private final AdminAuthManager authManager;
    private final AuthManager userAuthManager;

    @PostMapping("/login")
    @Operation(summary = "医生端 - 手机号密码登录")
    public Result<String> login(@Valid @RequestBody AdminLoginByPhoneAndPasswordRequest loginRequest,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        return authManager.loginByPhoneAndPasswordAndRoleId(
                loginRequest.getPhone(),
                loginRequest.getPassword(),
                DOCTOR_ROLE_ID,
                loginRequest.getRemember(),
                request,
                response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "医生端 - 刷新token")
    public Result<String> refresh(@CookieValue(value = "Refresh-token", required = false) String refreshToken,
                                   HttpServletResponse response) {
        return userAuthManager.refresh(refreshToken, response);
    }

    @PostMapping("/logout")
    @Operation(summary = "医生端 - 登出")
    public Result<String> logout(@RequestAttribute("currentAccountId") Long accountId) {
        return userAuthManager.logout(accountId);
    }

    @GetMapping("/current")
    @Operation(summary = "医生端 - 获取当前登录用户信息")
    public Result<AdminResponseSimple> getCurrentUser(@RequestAttribute("currentAccountId") Long currentAccountId) {
        return authManager.getCurrentAdminBaseInfo(currentAccountId);
    }
}
