package com.yirancrazy.smartmedical.controller.admin;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.manager.AccountManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AccountDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员账户控制器
 * @Datetime: 2026-03-06
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/admin/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "管理员账户管理", description = "管理员账户管理控制器")
public class AdminAccountControllerV1 {

    private final AccountManager accountManager;

    @GetMapping("/detail")
    @Operation(summary = "根据用户名、角色ID、是否启用分页查询账户详情")
    public Result<PageInfo<AccountDetailResponse>> listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "角色ID") @RequestParam(required = false) Long roleId,
            @Parameter(description = "是否启用") @RequestParam(required = false) Boolean enabled,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        return accountManager.listAccountDetailResponseByUsernameAndRoleIdAndEnabledAndPage(username, roleId, enabled, pageNum, pageSize);
    }
}
