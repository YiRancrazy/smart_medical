package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.RegistrationScheduleManager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.vo.registration.confirm.RegistrationConfirmTime;
import com.yirancrazy.smartmedical.pojo.vo.registration.confirm.RegistrationDateAndRemainQuotaVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班控制器
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Slf4j
@RestController
@RequestMapping("api/user/v1/registration/schedule")
@RequiredArgsConstructor
@Tag(name = "挂号排班管理", description = "挂号排班相关接口")
public class UserRegistrationScheduleControllerV1 {

    private final RegistrationScheduleManager registrationScheduleManager;

    @GetMapping("/doctor/recent")
    @Operation(summary = "获取最近七天该医生的排班信息", description = "获取最近七天该医生的排班信息")
    public Result<List<RegistrationDateAndRemainQuotaVo>> getDoctorRegistrationDateByDoctorId(@RequestParam("doctorId") Long doctorId){
        return registrationScheduleManager.listRegistrationsByDoctorIdAndMaxAdvanceDays(doctorId);
    }

    /**
     * 获取该医生在指定日期的排班信息
     * @param doctorId 医生ID
     * @param date 日期
     * @return 该医生在指定日期的排班信息
     */
    @GetMapping("/time")
    @Operation(summary = "获取该医生在指定日期的排班信息", description = "获取该医生在指定日期的排班信息")
    public Result<List<RegistrationConfirmTime>> getRegistrationScheduleByDoctorIdAndDate(@RequestParam("doctorId") Long doctorId, @RequestParam("date") LocalDate date){
        return registrationScheduleManager.getRegistrationScheduleByDoctorIdAndDate(doctorId, date);
    }

    /**
     * 获取挂号价格
     * @param registrationScheduleId 挂号排班ID
     * @return 挂号价格
     */
    @GetMapping("/price")
    @Operation(summary = "获取挂号价格", description = "获取挂号价格")
    public Result<Integer> getRegistrationPriceByRegistrationScheduleId(@RequestParam("registrationScheduleId") Long registrationScheduleId){
        return  registrationScheduleManager.getRegistrationPriceByRegistrationScheduleId(registrationScheduleId);
    }
}
