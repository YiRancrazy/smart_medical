package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.UserManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.dto.user.request.UpdateUserProfileRequest;
import com.yirancrazy.smartmedical.pojo.vo.UserBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.UserProfileInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 用户信息查询接口
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
    @Operation(summary = "根据用户ID获取用户基本信息",
            description = "返回用户基础信息，含展示名 displayName（本人就诊卡姓名 > 账号默认昵称 > \"-\"）与本人就诊卡卡号 ownPatientCardSn")
    @Parameter(name = "userId", description = "用户ID", required = true)
    public Result<UserBaseInfo> getUserBaseInfoByAccountId(@RequestParam("userId") Long userId) {
        return userManager.getUserBaseInfoByUserId(userId);
    }

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户个人信息", description = "获取当前登录用户的姓名、身份证号、性别、住址及完善状态")
    public Result<UserProfileInfo> getUserProfile(
            @RequestAttribute("currentUserId") Long userId) {
        return userManager.getUserProfile(userId);
    }

    @PutMapping("/profile")
    @Operation(summary = "完善当前用户个人信息", description = "补充当前登录用户的姓名、身份证号、性别和住址")
    public Result<UserProfileInfo> updateUserProfile(
            @RequestAttribute("currentUserId") Long userId,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        return userManager.updateUserProfile(userId, request);
    }
}
