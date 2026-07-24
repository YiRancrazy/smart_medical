package com.yirancrazy.smartmedical.controller.admin;

import com.yirancrazy.smartmedical.manager.DashboardManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.DashboardStatsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员工作台统计
 * @Datetime: 2026-07-24 16:00
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("api/admin/v1/dashboard")
@Tag(name = "管理员-工作台", description = "工作台统计接口")
public class AdminDashboardControllerV1 {

    private final DashboardManager dashboardManager;

    @GetMapping("/stats")
    @Operation(summary = "管理员端 - 获取工作台统计数据", description = "F19: 今日挂号/待就诊/就诊中/待发药 + 库存预警/新处方待处理")
    public Result<DashboardStatsResponse> getStats() {
        return dashboardManager.getStats();
    }
}
