package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.AuthManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.po.PhoneAndPasswordLoginRequest;
import com.yirancrazy.smartmedical.pojo.vo.LoginVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
@Tag(name = "账号管理", description = "账号相关接口")
public class UserAuthControllerV1 {

    private final AuthManager authManager;

    /**
     * 用户登录
     * @param phoneAndPasswordLoginRequest 手机号和密码
     * @return 登录成功
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public Result<LoginVo> login(@RequestBody PhoneAndPasswordLoginRequest phoneAndPasswordLoginRequest, HttpServletResponse response) {
        return authManager.login(phoneAndPasswordLoginRequest.getPhone(), phoneAndPasswordLoginRequest.getPassword(),response);
    }


    /**
     * 用户注册
     * @param phoneAndPasswordLoginRequest 手机号和密码
     * @return 注册成功
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    public Result<String> register(@RequestBody PhoneAndPasswordLoginRequest phoneAndPasswordLoginRequest) {
        return authManager.register(phoneAndPasswordLoginRequest.getPhone(), phoneAndPasswordLoginRequest.getPassword());
    }


    /**
     * 用户登出
     * @param userId 用户id
     * @return 登出成功
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出接口")
    public Result<String> logout(Long userId) {
        return authManager.logout(userId);
    }

}
