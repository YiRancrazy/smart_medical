package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.AdminManager;
import com.yirancrazy.smartmedical.pojo.Admin;
import com.yirancrazy.smartmedical.pojo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端 - 管理员查询接口
 * @Datetime: 2026-02-02 13:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/admin")
@RequiredArgsConstructor
@Tag(name = "管理员管理", description = "管理员相关接口")
public class UserAdminControllerV1 {

    private final AdminManager adminManager;

    @PostMapping("/add")
    @Operation(summary = "添加管理员", description = "添加新管理员")
    public Result<Integer> addAdmin(@RequestBody Admin admin) {
        return Result.success(adminManager.addAdmin(admin));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "根据ID获取管理员", description = "根据管理员ID获取管理员信息")
    @Parameter(name = "id", description = "管理员ID", required = true)
    public Result<Admin> getAdminById(@PathVariable Long id) {
        return Result.success(adminManager.getAdminById(id));
    }
}