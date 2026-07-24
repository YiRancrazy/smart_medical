package com.yirancrazy.smartmedical.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yirancrazy.smartmedical.mapper.DrugInventoryMapper;
import com.yirancrazy.smartmedical.mapper.RegistrationMapper;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.DashboardStatsResponse;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock private RegistrationMapper registrationMapper;
    @Mock private PrescriptionService prescriptionService;
    @Mock private DrugInventoryMapper drugInventoryMapper;

    @InjectMocks
    private DashboardManager dashboardManager;

    /**
     * happy path：6 次 count 调用返回不同值，验证 response 字段一一映射
     */
    @Test
    void getStats_shouldMapAllCountsToResponse() {
        // registrationMapper.selectCount 被调 3 次（今日挂号 / 待就诊 / 就诊中），按调用顺序返回
        when(registrationMapper.selectCount(any(QueryWrapper.class)))
                .thenReturn(12L)   // 今日挂号
                .thenReturn(3L)    // 待就诊
                .thenReturn(1L);   // 就诊中
        // prescriptionService.count 被调 2 次（待发药 / 新处方待处理）
        when(prescriptionService.count(any(QueryWrapper.class)))
                .thenReturn(5L)    // 待发药
                .thenReturn(2L);   // 新处方待处理
        // drugInventoryMapper.selectCount 被调 1 次（库存预警）
        when(drugInventoryMapper.selectCount(any(QueryWrapper.class)))
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
        when(registrationMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);
        when(prescriptionService.count(any(QueryWrapper.class))).thenReturn(0L);
        when(drugInventoryMapper.selectCount(any(QueryWrapper.class))).thenReturn(0L);

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
