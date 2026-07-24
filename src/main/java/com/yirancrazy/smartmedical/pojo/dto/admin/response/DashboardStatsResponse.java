package com.yirancrazy.smartmedical.pojo.dto.admin.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员工作台统计数据响应
 * @Datetime: 2026-07-24 16:00
 * @Version: 1.0
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    // 顶部统计卡
    private Long todayRegistrationCount;    // 今日挂号数
    private Long waitingVisitCount;         // 待就诊（含已报到）
    private Long inTreatmentCount;          // 就诊中
    private Long pendingDispenseCount;      // 待发药（处方已支付）

    // 待办提醒
    private Long pendingRefundCount;        // 待审核退号（当前无退号流程，恒为 0）
    private Long inventoryAlertCount;       // 库存预警（可用量 < 最小库存）
    private Long newPrescriptionCount;      // 新处方待处理（待支付）
}
