package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.UserManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.vo.UserBaseInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class UserUserControllerV1 {

    private final UserManager userManager;

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "根据ID获取用户", description = "根据用户ID获取用户信息")
    @Parameter(name = "id", description = "用户ID", required = true)
    public Result<User> getUserById(@PathVariable Long id) {
        return Result.success(userManager.getUserById(id));
    }

    @GetMapping("/baseinfo")
    @Operation(summary = "根据账号ID获取用户基本信息", description = "根据账号ID获取用户基本信息")
    @Parameter(name = "uid", description = "账号ID", required = true)
    public Result<UserBaseInfo> getUserBaseInfoByAccountId(@RequestParam("userId") Long userId) {
        return userManager.getUserBaseInfoByUserId(userId);
    }

    
}