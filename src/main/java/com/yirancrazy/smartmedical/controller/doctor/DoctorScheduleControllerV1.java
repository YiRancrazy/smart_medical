package com.yirancrazy.smartmedical.controller.doctor;

import com.yirancrazy.smartmedical.manager.DoctorManager;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.DoctorScheduleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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
     * @param doctorId 医生ID
     * @return 今日挂号列表
     */
    @Operation(summary = "医生端 - 今日排班列表")
    @GetMapping("/today")
    public Result<List<DoctorScheduleVO>> todaySchedule(@RequestParam Long doctorId) {
        List<Registration> list = doctorManager.listTodaySchedule(doctorId);
        List<DoctorScheduleVO> result = list.stream().map(this::toVO).collect(Collectors.toList());
        return Result.success(result);
    }

    private DoctorScheduleVO toVO(Registration reg) {
        DoctorScheduleVO vo = new DoctorScheduleVO();
        vo.setRegistrationId(reg.getId());
        vo.setStatus(reg.getStatus());
        vo.setStartTime(reg.getRegistrationTime());
        return vo;
    }
}