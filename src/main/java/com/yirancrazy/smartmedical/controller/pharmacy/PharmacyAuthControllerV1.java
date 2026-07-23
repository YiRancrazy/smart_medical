package com.yirancrazy.smartmedical.controller.pharmacy;

import com.yirancrazy.smartmedical.manager.AdminAuthManager;
import com.yirancrazy.smartmedical.manager.AuthManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.request.AdminLoginByPhoneAndPasswordRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminResponseSimple;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 药师端登录控制器
 * @Datetime: 2026-07-17 11:30
 * @Version: 1.0
 */

@RestController
@RequestMapping("/api/pharmacy/v1/auth")
@Tag(name = "药师端-登录控制器")
@RequiredArgsConstructor
public class PharmacyAuthControllerV1 {

    /** 药师角色 id（来自 role 表） */
    private static final Long PHARMACIST_ROLE_ID = 6L;

    private final AdminAuthManager authManager;
    private final AuthManager userAuthManager;

    @PostMapping("/login")
    @Operation(summary = "药师端 - 手机号密码登录")
    public Result<String> login(@RequestBody AdminLoginByPhoneAndPasswordRequest loginRequest,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        return authManager.loginByPhoneAndPasswordAndRoleId(
                loginRequest.getPhone(),
                loginRequest.getPassword(),
                PHARMACIST_ROLE_ID,
                loginRequest.getRemember(),
                request,
                response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "药师端 - 刷新token")
    public Result<String> refresh(@CookieValue("Refresh-token") String refreshToken,
                                   HttpServletResponse response) {
        return userAuthManager.refresh(refreshToken, response);
    }

    @PostMapping("/logout")
    @Operation(summary = "药师端 - 登出")
    public Result<String> logout(@RequestAttribute("currentAccountId") Long accountId) {
        return userAuthManager.logout(accountId);
    }

    @GetMapping("/current")
    @Operation(summary = "药师端 - 获取当前登录用户信息")
    public Result<AdminResponseSimple> getCurrentUser(@RequestAttribute("currentAccountId") Long currentAccountId) {
        return authManager.getCurrentAdminBaseInfo(currentAccountId);
    }
}
