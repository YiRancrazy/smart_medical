package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.AuthManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.request.ChangePasswordRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.request.ForgotPasswordRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.request.PhoneAndPasswordLoginRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.request.SmsCodeRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.request.SmsRegisterRequest;
import com.yirancrazy.smartmedical.pojo.vo.LoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 认证接口：登录 / 注册 / 验证码 / 重置密码
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
     * 发送短信验证码（注册 / 验证码登录，60秒内不可重复发送）
     * @param smsCodeRequest 手机号和场景
     * @return 发送结果
     */
    @PostMapping("/sms-code")
    @Operation(summary = "用户端 - 发送短信验证码", description = "scene=register 发送注册验证码（账号不存在才可发）；scene=login 发送登录验证码（账号必须已存在）")
    public Result<String> sendSmsCode(@Valid @RequestBody SmsCodeRequest smsCodeRequest) {
        return authManager.sendSmsCode(smsCodeRequest.getPhone(), smsCodeRequest.getScene());
    }

    /**
     * 用户验证码登录（手机号 + 短信验证码，登录成功自动签发 token）
     * @param smsRegisterRequest 手机号和验证码
     * @param response 用于签发 token
     * @return 登录结果（返回 LoginVo）
     */
    @PostMapping("/login-by-code")
    @Operation(summary = "用户端 - 验证码登录", description = "手机号+短信验证码登录，登录成功后自动签发 token")
    public Result<LoginVo> loginByCode(@Valid @RequestBody SmsRegisterRequest smsRegisterRequest, HttpServletResponse response) {
        return authManager.loginByCode(smsRegisterRequest.getPhone(), smsRegisterRequest.getCode(), response);
    }

    /**
     * 用户注册（手机号 + 短信验证码，注册成功自动登录）
     * @param smsRegisterRequest 手机号和验证码
     * @param response 用于签发 token
     * @return 注册结果（自动登录返回 LoginVo）
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "手机号+短信验证码注册，注册成功后自动登录")
    public Result<LoginVo> register(@Valid @RequestBody SmsRegisterRequest smsRegisterRequest, HttpServletResponse response) {
        return authManager.register(smsRegisterRequest.getPhone(), smsRegisterRequest.getCode(), response);
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
     * 用户忘记密码重置（未登录，需短信验证码）
     * @param forgotPasswordRequest 手机号、验证码和新密码
     * @return 重置结果
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "用户端 - 忘记密码", description = "未登录重置密码，需短信验证码校验")
    public Result<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        return authManager.forgotPassword(forgotPasswordRequest.getPhone(), forgotPasswordRequest.getCode(), forgotPasswordRequest.getPassword());
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
