package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.UserPatientRelation;
import com.yirancrazy.smartmedical.service.PatientService;
import com.yirancrazy.smartmedical.service.UserPatientRelationService;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 患者管理层
 * @Author: YiRanCrazy@gmail.com
 * @Description: 患者管理层
 * @Datetime: 2026-03-01 15:21
 * @Version: 1.0
 */

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
        List<UserPatientRelation> relations = userPatientRelationService.getUserPatientRelationsByUserId(currentUserId);
        // 仅保留已授权关系或本人关系，防止越权读取他人病历/处方/挂号
        List<UserPatientRelation> authorizedRelations = relations.stream()
                .filter(r -> r.getIsAuthorized() != null && r.getIsAuthorized() == 1
                        || currentUserId.equals(r.getPatientUserId()))
                .toList();
        if (patientCardId != null) {
            Patient patient = patientService.getPatientByPatientCardId(patientCardId);
            if (patient == null) {
                return List.of();
            }
            Long targetUserId = patient.getUserId();
            boolean allowed = authorizedRelations.stream()
                    .anyMatch(relation -> relation.getPatientUserId().equals(targetUserId));
            return allowed ? List.of(targetUserId) : List.of();
        }
        return authorizedRelations.stream()
                .map(UserPatientRelation::getPatientUserId)
                .distinct()
                .toList();
    }
}