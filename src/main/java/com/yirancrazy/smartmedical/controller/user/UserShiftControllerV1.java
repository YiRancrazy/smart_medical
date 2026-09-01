package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.ShiftManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.Shift;
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
@RequestMapping("api/user/v1/shift")
@RequiredArgsConstructor
@Tag(name = "班次管理", description = "班次相关接口")
public class UserShiftControllerV1 {

    private final ShiftManager shiftManager;

    @PostMapping("/add")
    @Operation(summary = "添加班次", description = "添加新班次")
    public Result<Integer> addShift(@RequestBody Shift shift) {
        return Result.success(shiftManager.addShift(shift));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "根据ID获取班次", description = "根据班次ID获取班次信息")
    @Parameter(name = "id", description = "班次ID", required = true)
    public Result<Shift> getShiftById(@PathVariable String id) {
        return Result.success(shiftManager.getShiftById(Long.parseLong(id)));
    }
}