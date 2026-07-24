package com.yirancrazy.smartmedical.manager;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.PrescriptionStatus;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.mapper.DrugInventoryMapper;
import com.yirancrazy.smartmedical.mapper.RegistrationMapper;
import com.yirancrazy.smartmedical.pojo.DrugInventory;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.DashboardStatsResponse;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员工作台统计 Manager
 * @Datetime: 2026-07-24 16:00
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
@Slf4j
public class DashboardManager {

    private final RegistrationMapper registrationMapper;
    private final PrescriptionService prescriptionService;
    private final DrugInventoryMapper drugInventoryMapper;

    /**
     * 查询工作台统计数据
     * @return 7 项统计计数
     */
    public Result<DashboardStatsResponse> getStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = today.plusDays(1).atStartOfDay();

        // 今日挂号：registration_time 在今天（排除已取消）
        Long todayReg = registrationMapper.selectCount(
                new QueryWrapper<Registration>()
                        .ge("registration_time", dayStart)
                        .lt("registration_time", dayEnd)
                        .ne("status", RegistrationStatusEnum.CANCELED.getCode()));

        // 待就诊：status IN (1 待就诊, 5 已报到)
        Long waitingVisit = registrationMapper.selectCount(
                new QueryWrapper<Registration>()
                        .in("status",
                                RegistrationStatusEnum.SUCCESS.getCode(),
                                RegistrationStatusEnum.REPORTED.getCode()));

        // 就诊中：status = 6
        Long inTreatment = registrationMapper.selectCount(
                new QueryWrapper<Registration>()
                        .eq("status", RegistrationStatusEnum.IN_TREATMENT.getCode()));

        // 待发药：处方 status = 1（已支付）
        Long pendingDispense = prescriptionService.count(
                new QueryWrapper<Prescription>()
                        .eq("status", PrescriptionStatus.PAID.getCode()));

        // 库存预警：available_quantity < min_stock
        Long inventoryAlert = drugInventoryMapper.selectCount(
                new QueryWrapper<DrugInventory>()
                        .apply("available_quantity < min_stock"));

        // 新处方待处理：处方 status = 0（待支付）
        Long newPrescription = prescriptionService.count(
                new QueryWrapper<Prescription>()
                        .eq("status", PrescriptionStatus.PENDING_PAYMENT.getCode()));

        // ponytail: 待审核退号当前无退号流程，恒为 0；后续若加退号审批表再补
        DashboardStatsResponse response = new DashboardStatsResponse(
                todayReg, waitingVisit, inTreatment, pendingDispense,
                0L, inventoryAlert, newPrescription);
        return Result.success(response);
    }
}
