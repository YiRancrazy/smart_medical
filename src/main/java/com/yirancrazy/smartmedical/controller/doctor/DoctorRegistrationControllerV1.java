package com.yirancrazy.smartmedical.controller.doctor;

import com.yirancrazy.smartmedical.manager.DoctorManager;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.WaitingPatientVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生端 - 挂号/叫号
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Tag(name = "医生端 - 挂号/叫号")
@RestController
@RequestMapping("/doctor/v1/registration")
@RequiredArgsConstructor
public class DoctorRegistrationControllerV1 {

    private final DoctorManager doctorManager;

    /**
     * 医生端 - 待叫号列表
     * @param doctorId 医生ID
     * @return 待叫号挂号列表
     */
    @Operation(summary = "医生端 - 待叫号列表")
    @GetMapping("/waiting")
    public Result<List<WaitingPatientVO>> waiting(@RequestParam Long doctorId) {
        List<Registration> list = doctorManager.listWaiting(doctorId);
        List<WaitingPatientVO> result = list.stream().map(this::toWaitingVO).collect(Collectors.toList());
        return Result.success(result);
    }

    /**
     * 医生端 - 叫号接诊
     * @param regId 挂号记录ID
     * @param doctorId 医生ID
     */
    @Operation(summary = "医生端 - 叫号接诊")
    @PostMapping("/{id}/call")
    public Result<Void> call(@PathVariable("id") Long regId,
                             @RequestParam Long doctorId) {
        doctorManager.callPatient(regId, doctorId);
        return Result.success(null);
    }

    private WaitingPatientVO toWaitingVO(Registration reg) {
        WaitingPatientVO vo = new WaitingPatientVO();
        vo.setRegistrationId(reg.getId());
        vo.setPatientId(reg.getUserId());
        vo.setCheckInTime(reg.getCheckInTime());
        return vo;
    }
}