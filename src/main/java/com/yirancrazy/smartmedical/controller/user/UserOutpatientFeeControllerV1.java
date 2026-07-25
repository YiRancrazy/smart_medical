package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.OutpatientFeeManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.OutpatientFeeItemResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端 - 门诊费用 Controller
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端门诊费用列表查询
 * @Datetime: 2026-07-25 14:00
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("api/user/v1/outpatient-fee")
@Tag(name = "用户端 - 门诊费用", description = "用户端门诊费用管理")
public class UserOutpatientFeeControllerV1 {

    private final OutpatientFeeManager outpatientFeeManager;

    /**
     * 用户端 - 查询当前用户的门诊费用列表
     * @param userId 当前用户ID
     * @param current 当前页码
     * @param size 每页大小
     * @return 门诊费用分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "用户端 - 查询当前用户的门诊费用列表", description = "用户端 - 查询当前用户的门诊费用列表")
    public Result<PageResult<OutpatientFeeItemResponse>> listMyOutpatientFees(
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(required = false) Integer current,
            @RequestParam(required = false) Integer size) {
        return outpatientFeeManager.listMyOrders(userId, current, size);
    }
}
