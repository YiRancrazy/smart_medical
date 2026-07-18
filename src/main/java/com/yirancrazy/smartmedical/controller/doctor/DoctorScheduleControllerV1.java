package com.yirancrazy.smartmedical.controller.doctor;

import com.yirancrazy.smartmedical.manager.DoctorManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.DoctorScheduleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
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
     * 医生端 - 今日排班列表
     * @param doctorId 医生ID（JWT 自动注入）
     */
    @Operation(summary = "医生端 - 今日排班列表")
    @GetMapping("/today")
    public Result<List<DoctorScheduleVO>> todaySchedule(@RequestAttribute("currentDoctorId") Long doctorId) {
        return Result.success(doctorManager.listTodaySchedule(doctorId));
    }
}
