package com.yirancrazy.smartmedical.controller.admin;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.manager.ShiftManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.response.ShiftSimpleResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-06 12:16
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@Tag(name = "班次管理")
@RequestMapping("/api/admin/v1/shift")
public class AdminShiftControllerV1 {
    private final ShiftManager shiftManager;

    /**
     * 获取所有班次信息
     * @return 班次信息
     */
    @RequestMapping("/list")
    public Result<List<ShiftSimpleResponse>> listShiftsSimpleResponse(){
        return shiftManager.listShiftsSimpleResponse();
    }
}