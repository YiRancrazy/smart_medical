package com.yirancrazy.smartmedical.controller.admin;

import com.yirancrazy.smartmedical.manager.UserManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理员端 - 用户增删（收归 admin 角色，原误置于用户端导致 IDOR）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户新增 / 删除仅管理员可调用（/api/admin/v1 由 SecurityConfig 限 hasRole("admin")）
 * @Datetime: 2026-09-05 12:00
 * @Version: 1.0
 */

@Tag(name = "管理员端 - 用户管理", description = "用户新增/删除")
@RestController
@RequestMapping("api/admin/v1/user")
@RequiredArgsConstructor
public class AdminUserControllerV1 {

    private final UserManager userManager;

    @PostMapping("/add")
    @Operation(summary = "管理员端 - 添加用户")
    public Result<Integer> addUser(@RequestBody User user) {
        return Result.success(userManager.addUser(user));
    }

    @DeleteMapping("/{id:\\d+}")
    @Operation(summary = "管理员端 - 删除用户")
    @Parameter(name = "id", description = "用户ID", required = true)
    public Result<Integer> deleteUserById(@PathVariable Long id) {
        return Result.success(userManager.deleteUserById(id));
    }
}