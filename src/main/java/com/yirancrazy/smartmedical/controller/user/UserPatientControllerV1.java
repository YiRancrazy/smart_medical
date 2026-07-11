package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.service.PatientService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 患者管理
 * @Datetime: 2026-04-04 10:09
 * @Version: 1.0
 */

@RestController
@RequiredArgsConstructor
@Tag(name = "患者管理", description = "患者管理")
@RequestMapping("api/user/v1/patient")
public class UserPatientControllerV1 {
    private final PatientService patientService;


}