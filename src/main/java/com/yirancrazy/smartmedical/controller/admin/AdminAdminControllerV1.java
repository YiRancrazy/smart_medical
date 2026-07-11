package com.yirancrazy.smartmedical.controller.admin;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.manager.AdminManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.AdminAdminSimpleResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-05 19:25
 * @Version: 1.0
 */

@RequiredArgsConstructor
@RestController
@Tag(name = "管理管 - 管理员管理")
@RequestMapping("/api/admin/v1/admin")
public class AdminAdminControllerV1 {
    private final AdminManager adminManager;

    /**
     * 根据管理员id获取管理员简单响应
     * @param id 管理员id
     * @return 管理员简单响应
     */
    @GetMapping("/simple/{id}")
    @Operation(summary = "根据管理员id获取管理员简单响应")
    public Result<AdminAdminSimpleResponse> getAdminAdminSimpleResponseByAdminId(@PathVariable  Long id){
        return adminManager.getAdminAdminSimpleResponseByAdminId(id);
    }

    /**
     * 获取所有管理员简单响应
     * @return 所有管理员简单响应
     */
    @GetMapping("/list/simple")
    @Operation(summary = "获取所有管理员简单响应")
    public Result<List<AdminAdminSimpleResponse>> listAdminAdminSimpleResponseByAdminIds(){
        return adminManager.listAdminAdminSimpleResponse();
    }



    /**
     * 分页获取所有管理员简单响应
     * @param current 当前页
     * @param size 每页数量
     * @return 所有管理员简单响应
     */
    @GetMapping("/list/simple/{current}/{size}")
    @Operation(summary = "分页获取所有管理员简单响应")
    public Result<PageResult<AdminAdminSimpleResponse>> listAdminAdminSimpleResponseByAdminIds(@PathVariable  Integer current, @PathVariable  Integer size){
        return adminManager.listAdminAdminSimpleResponseByPage(current,size);
    }

}