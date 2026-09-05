package com.yirancrazy.smartmedical.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.DashboardStatsResponse;
import com.yirancrazy.smartmedical.service.DrugInventoryService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * DashboardManager#getStats 单测
 * 覆盖：7 项统计计数正确映射到 DashboardStatsResponse。
 * @Author: YiRanCrazy@gmail.com
 * @Description: 工作台统计 Manager 单测
 * @Datetime: 2026-07-24 17:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class DashboardManagerTest {

    @Mock private RegistrationService registrationService;
    @Mock private PrescriptionService prescriptionService;
    @Mock private DrugInventoryService drugInventoryService;

    @InjectMocks
    private DashboardManager dashboardManager;

    /**
     * happy path：6 次 count 调用返回不同值，验证 response 字段一一映射
     */
    @Test
    void getStats_shouldMapAllCountsToResponse() {
        when(registrationService.countTodayRegistrations(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(12L);   // 今日挂号
        when(registrationService.countByStatuses(any()))
                .thenReturn(3L);    // 待就诊
        when(registrationService.countByStatus(any()))
                .thenReturn(1L);    // 就诊中
        // prescriptionService.count 被调 2 次（待发药 / 新处方待处理）
        when(prescriptionService.count(any(QueryWrapper.class)))
                .thenReturn(5L)    // 待发药
                .thenReturn(2L);   // 新处方待处理
        when(drugInventoryService.countLowStock())
                .thenReturn(4L);   // 库存预警

        Result<DashboardStatsResponse> result = dashboardManager.getStats();

        assertNotNull(result);
        DashboardStatsResponse data = result.getData();
        assertNotNull(data);
        assertEquals(12L, data.getTodayRegistrationCount());
        assertEquals(3L, data.getWaitingVisitCount());
        assertEquals(1L, data.getInTreatmentCount());
        assertEquals(5L, data.getPendingDispenseCount());
        // pendingRefundCount 恒为 0（当前无退号流程）
        assertEquals(0L, data.getPendingRefundCount());
        assertEquals(4L, data.getInventoryAlertCount());
        assertEquals(2L, data.getNewPrescriptionCount());
    }

    /**
     * 边界：所有 count 返回 0，response 全 0 不报错
     */
    @Test
    void getStats_allZero_shouldReturnZeroResponse() {
        when(registrationService.countTodayRegistrations(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);
        when(registrationService.countByStatuses(any())).thenReturn(0L);
        when(registrationService.countByStatus(any())).thenReturn(0L);
        when(prescriptionService.count(any(QueryWrapper.class))).thenReturn(0L);
        when(drugInventoryService.countLowStock()).thenReturn(0L);

        Result<DashboardStatsResponse> result = dashboardManager.getStats();

        DashboardStatsResponse data = result.getData();
        assertEquals(0L, data.getTodayRegistrationCount());
        assertEquals(0L, data.getWaitingVisitCount());
        assertEquals(0L, data.getInTreatmentCount());
        assertEquals(0L, data.getPendingDispenseCount());
        assertEquals(0L, data.getPendingRefundCount());
        assertEquals(0L, data.getInventoryAlertCount());
        assertEquals(0L, data.getNewPrescriptionCount());
    }
}
