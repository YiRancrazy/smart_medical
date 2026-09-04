package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.AuthManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.request.ChangePasswordRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.request.PhoneAndPasswordLoginRequest;
import com.yirancrazy.smartmedical.pojo.vo.LoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/auth")
@RequiredArgsConstructor
@Tag(name = "用户账号管理", description = "账号相关接口")
public class UserAuthControllerV1 {

    private final AuthManager authManager;

    /**
     * 用户登录
     * @param phoneAndPasswordLoginRequest 手机号和密码
     * @return 登录成功
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public Result<LoginVo> login(@Valid @RequestBody PhoneAndPasswordLoginRequest phoneAndPasswordLoginRequest, HttpServletResponse response) {
        return authManager.login(phoneAndPasswordLoginRequest.getPhone(), phoneAndPasswordLoginRequest.getPassword(),response);
    }


    /**
     * 用户注册
     * @param phoneAndPasswordLoginRequest 手机号和密码
     * @return 注册成功
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    public Result<String> register(@Valid @RequestBody PhoneAndPasswordLoginRequest phoneAndPasswordLoginRequest) {
        return authManager.register(phoneAndPasswordLoginRequest.getPhone(), phoneAndPasswordLoginRequest.getPassword());
    }


    /**
     * 用户登出
     * @param userId 用户id
     * @return 登出成功
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出接口")
    public Result<String> logout(@RequestAttribute("currentAccountId") Long accountId) {
        return authManager.logout(accountId);
    }

    @PostMapping("/refresh")
    @Operation(summary = "用户端 - 刷新token", description = "刷新access token")
    public Result<String> refresh(@CookieValue(value = "Refresh-token", required = false) String refreshToken,
                                   HttpServletResponse response) {
        return authManager.refresh(refreshToken, response);
    }

    /**
     * 用户忘记密码重置（未登录，需先通过滑块验证）
     * @param phoneAndPasswordLoginRequest 手机号和新密码
     * @return 重置结果
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "用户端 - 忘记密码", description = "未登录重置密码，需先通过滑块验证")
    public Result<String> forgotPassword(@Valid @RequestBody PhoneAndPasswordLoginRequest phoneAndPasswordLoginRequest) {
        return authManager.forgotPassword(phoneAndPasswordLoginRequest.getPhone(), phoneAndPasswordLoginRequest.getPassword());
    }

    /**
     * 用户登录态修改密码
     * @param changePasswordRequest 原密码和新密码
     * @param accountId 当前登录账号ID
     * @return 修改结果
     */
    @PostMapping("/change-password")
    @Operation(summary = "用户端 - 修改密码", description = "登录态修改登录密码")
    public Result<String> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest,
                                         @RequestAttribute("currentAccountId") Long accountId) {
        return authManager.changePassword(accountId, changePasswordRequest.getOldPassword(), changePasswordRequest.getNewPassword());
    }

}
