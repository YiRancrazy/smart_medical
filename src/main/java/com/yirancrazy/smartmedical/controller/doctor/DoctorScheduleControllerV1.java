package com.yirancrazy.smartmedical.controller.doctor;

import com.yirancrazy.smartmedical.manager.DoctorManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.DoctorScheduleViewVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生端 - 排班查询
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Tag(name = "医生端 - 排班")
@RestController
@RequestMapping("/api/doctor/v1/schedule")
@RequiredArgsConstructor
public class DoctorScheduleControllerV1 {

    private final DoctorManager doctorManager;

    /**
     * 医生端 - 月度排班列表（日/周/月视图共用数据源，纯排班信息）
     * @param doctorId 医生ID（JWT 自动注入）
     * @param month 月份，格式 yyyy-MM
     */
    @Operation(summary = "医生端 - 月度排班列表")
    @GetMapping("/month")
    public Result<List<DoctorScheduleViewVO>> monthSchedule(@RequestAttribute("currentDoctorId") Long doctorId,
                                                            @RequestParam String month) {
        return doctorManager.listMonthSchedule(doctorId, month);
    }
}
