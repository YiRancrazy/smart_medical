package com.yirancrazy.smartmedical.controller.pharmacy;

import com.yirancrazy.smartmedical.manager.AdminAuthManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.request.AdminLoginByPhoneAndPasswordRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
