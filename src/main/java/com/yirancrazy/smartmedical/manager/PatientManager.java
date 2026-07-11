package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.service.PatientService;
import lombok.RequiredArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 患者管理层
 * @Datetime: 2026-03-01 15:21
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class PatientManager {
    private final PatientService patientService;
}