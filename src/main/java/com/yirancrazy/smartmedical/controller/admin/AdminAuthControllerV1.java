package com.yirancrazy.smartmedical.controller.admin;

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
 * @Description: 管理员登录控制器
 * @Datetime: 2026-03-05 17:10
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/admin/v1/auth")
@RequiredArgsConstructor
@Tag(name = "管理员登录控制器", description = "管理员登录控制器")
public class AdminAuthControllerV1 {

    private final AdminAuthManager authManager;
    private final AuthManager userAuthManager;

    /**
     * 管理员手机号密码登录
     * @param loginRequest 管理员根据手机号和密码登录的请求
     * @param request 请求
     * @param response 响应
     * @return 结果
     */
    @PostMapping("/login")
    @Operation(summary = "管理员手机号密码登录")
    public Result<String> login(@RequestBody AdminLoginByPhoneAndPasswordRequest loginRequest,HttpServletRequest request,HttpServletResponse response) {
        return authManager.loginByPhoneAndPassword(loginRequest.getPhone(),
                loginRequest.getPassword(),
                loginRequest.getRemember(),
                request,
                response);
    }

    /**
     * 获取当前登录管理员信息
     * @param currentAccountId JwtAuthenticationFilter 注入的 accountId
     * @return 管理员信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前登录管理员信息")
    public Result<AdminResponseSimple> getCurrentAdminBaseInfo(@RequestAttribute("currentAccountId") Long currentAccountId) {
        return authManager.getCurrentAdminBaseInfo(currentAccountId);
    }

    @PostMapping("/refresh")
    @Operation(summary = "管理员端 - 刷新token")
    public Result<String> refresh(@CookieValue("Refresh-token") String refreshToken,
                                   HttpServletResponse response) {
        return userAuthManager.refresh(refreshToken, response);
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员端 - 登出")
    public Result<String> logout(@RequestAttribute("currentAccountId") Long accountId) {
        return userAuthManager.logout(accountId);
    }

}