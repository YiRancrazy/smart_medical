package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 患者管理层
 * @Author: YiRanCrazy@gmail.com
 * @Description: 患者管理层（可访问患者集合的薄门面，逻辑下沉至 UserPatientRelationService）
 * @Datetime: 2026-03-01 15:21
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class PatientManager {

    private final PatientService patientService;
    private final UserPatientRelationService userPatientRelationService;

    /**
     * 获取当前账号下可访问的患者 userId 列表
     * @param currentUserId 当前登录用户id
     * @param patientCardId 就诊卡id（为 null 时返回全部关联患者）
     * @return 患者 userId 列表；patientCardId 不合法或无权限时返回空列表
     */
    public List<Long> getAccessiblePatientUserIds(Long currentUserId, Long patientCardId) {
        if (patientCardId == null) {
            return userPatientRelationService.listAccessiblePatientUserIds(currentUserId);
        }
        Patient patient = patientService.getPatientByPatientCardId(patientCardId);
        if (patient == null || patient.getUserId() == null) {
            return List.of();
        }
        return userPatientRelationService.hasAuthorization(currentUserId, patient.getUserId())
                ? List.of(patient.getUserId())
                : List.of();
    }
}
