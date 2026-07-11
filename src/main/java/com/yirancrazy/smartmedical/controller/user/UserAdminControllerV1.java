package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.AdminManager;
import com.yirancrazy.smartmedical.pojo.Admin;
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
@RequestMapping("api/user/v1/admin")
@RequiredArgsConstructor
@Tag(name = "管理员管理", description = "管理员相关接口")
public class UserAdminControllerV1 {

    private final AdminManager adminManager;

    @PostMapping("/add")
    @Operation(summary = "添加管理员", description = "添加新管理员")
    public int addAdmin(@RequestBody Admin admin) {
        return adminManager.addAdmin(admin);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取管理员", description = "根据管理员ID获取管理员信息")
    @Parameter(name = "id", description = "管理员ID", required = true)
    public Admin getAdminById(@PathVariable String id) {
        return adminManager.getAdminById(Long.parseLong(id));
    }
}
