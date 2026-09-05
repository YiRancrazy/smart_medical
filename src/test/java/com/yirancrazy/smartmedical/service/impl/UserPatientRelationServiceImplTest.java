package com.yirancrazy.smartmedical.service.impl;

import com.yirancrazy.smartmedical.mapper.UserPatientRelationMapper;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.pojo.UserPatientRelation;
import com.yirancrazy.smartmedical.service.PatientService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * UserPatientRelationServiceImpl#getAccessiblePatientUserIds 单测
 * 覆盖：所有已添加就诊人（含本人、is_authorized 不再参与判断）、patientCardId 过滤、患者不存在。
 * @Author: YiRanCrazy@gmail.com
 * @Description: 患者可访问范围单测（原 PatientManager 逻辑下沉）
 * @Datetime: 2026-09-05 10:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserPatientRelationServiceImplTest {

    @Mock private UserPatientRelationMapper userPatientRelationMapper;
    @Mock private PatientService patientService;

    @InjectMocks
    private UserPatientRelationServiceImpl userPatientRelationService;

    private static final Long CURRENT_USER_ID = 7L;

    /**
     * patientCardId=null：返回所有已添加就诊人的 patientUserId（去重），不再按 is_authorized 过滤
     */
    @Test
    void getAccessiblePatientUserIds_nullCardId_returnsAllRelations() {
        UserPatientRelation self = buildRelation(7L, 0);
        UserPatientRelation otherAuth = buildRelation(8L, 1);
        UserPatientRelation otherNoAuth = buildRelation(9L, 0);

        when(userPatientRelationMapper.selectList(any())).thenReturn(List.of(self, otherAuth, otherNoAuth));

        List<Long> result = userPatientRelationService.getAccessiblePatientUserIds(CURRENT_USER_ID, null);

        assertEquals(3, result.size());
        assertTrue(result.contains(7L));
        assertTrue(result.contains(8L));
        assertTrue(result.contains(9L));
    }

    /**
     * isAuthorized 不再参与判断：即使 isAuthorized=null 的关系也可访问
     */
    @Test
    void getAccessiblePatientUserIds_nullAuthorization_notFiltered() {
        UserPatientRelation nullAuth = new UserPatientRelation();
        nullAuth.setPatientUserId(10L);
        nullAuth.setIsAuthorized(null);

        when(userPatientRelationMapper.selectList(any())).thenReturn(List.of(nullAuth));

        List<Long> result = userPatientRelationService.getAccessiblePatientUserIds(CURRENT_USER_ID, null);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0));
    }

    /**
     * patientCardId 指向已添加就诊人 → 返回该患者 userId
     */
    @Test
    void getAccessiblePatientUserIds_validCardIdAuthorized_returnsSingle() {
        UserPatientRelation relation = buildRelation(8L, 0);
        Patient patient = new Patient();
        patient.setUserId(8L);

        when(userPatientRelationMapper.selectList(any())).thenReturn(List.of(relation));
        when(patientService.getPatientByPatientCardId(100L)).thenReturn(patient);

        List<Long> result = userPatientRelationService.getAccessiblePatientUserIds(CURRENT_USER_ID, 100L);

        assertEquals(1, result.size());
        assertEquals(8L, result.get(0));
    }

    /**
     * patientCardId 指向未添加的就诊人 → 返回空列表
     */
    @Test
    void getAccessiblePatientUserIds_cardIdUnauthorized_returnsEmpty() {
        UserPatientRelation relation = buildRelation(8L, 0);
        Patient patient = new Patient();
        patient.setUserId(9L);  // 9L 不在关系列表

        when(userPatientRelationMapper.selectList(any())).thenReturn(List.of(relation));
        when(patientService.getPatientByPatientCardId(100L)).thenReturn(patient);

        List<Long> result = userPatientRelationService.getAccessiblePatientUserIds(CURRENT_USER_ID, 100L);

        assertTrue(result.isEmpty());
    }

    /**
     * patientCardId 指向不存在的患者 → 返回空列表
     */
    @Test
    void getAccessiblePatientUserIds_patientNotFound_returnsEmpty() {
        when(userPatientRelationMapper.selectList(any())).thenReturn(List.of(buildRelation(8L, 1)));
        when(patientService.getPatientByPatientCardId(999L)).thenReturn(null);

        List<Long> result = userPatientRelationService.getAccessiblePatientUserIds(CURRENT_USER_ID, 999L);

        assertTrue(result.isEmpty());
    }

    /**
     * 本人关系（patientUserId == currentUserId）始终可访问
     */
    @Test
    void getAccessiblePatientUserIds_selfRelationAlwaysAccessible() {
        UserPatientRelation self = buildRelation(CURRENT_USER_ID, 0);

        when(userPatientRelationMapper.selectList(any())).thenReturn(List.of(self));

        List<Long> result = userPatientRelationService.getAccessiblePatientUserIds(CURRENT_USER_ID, null);

        assertEquals(1, result.size());
        assertEquals(CURRENT_USER_ID, result.get(0));
    }

    // ===== 辅助构造 =====

    private UserPatientRelation buildRelation(Long patientUserId, Integer isAuthorized) {
        UserPatientRelation relation = new UserPatientRelation();
        relation.setPatientUserId(patientUserId);
        relation.setIsAuthorized(isAuthorized);
        return relation;
    }
}
