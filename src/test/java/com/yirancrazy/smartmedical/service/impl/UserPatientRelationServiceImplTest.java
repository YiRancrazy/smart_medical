package com.yirancrazy.smartmedical.service.impl;

import com.yirancrazy.smartmedical.mapper.UserPatientRelationMapper;
import com.yirancrazy.smartmedical.pojo.UserPatientRelation;
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
 * UserPatientRelationServiceImpl#listAccessiblePatientUserIds 单测
 * 覆盖：所有已添加就诊人（含本人、is_authorized 不再参与判断）与去重。
 * @Author: YiRanCrazy@gmail.com
 * @Description: 患者可访问范围单测
 * @Datetime: 2026-09-05 10:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class UserPatientRelationServiceImplTest {

    @Mock private UserPatientRelationMapper userPatientRelationMapper;

    @InjectMocks
    private UserPatientRelationServiceImpl userPatientRelationService;

    private static final Long CURRENT_USER_ID = 7L;

    /**
     * 返回所有已添加就诊人的 patientUserId（去重），不再按 is_authorized 过滤
     */
    @Test
    void listAccessiblePatientUserIds_returnsDistinctPatientUserIds() {
        UserPatientRelation self = buildRelation(7L, 0);
        UserPatientRelation otherAuth = buildRelation(8L, 1);
        UserPatientRelation duplicate = buildRelation(8L, 0);

        when(userPatientRelationMapper.selectList(any())).thenReturn(List.of(self, otherAuth, duplicate));

        List<Long> result = userPatientRelationService.listAccessiblePatientUserIds(CURRENT_USER_ID);

        assertEquals(List.of(7L, 8L), result);
    }

    /**
     * isAuthorized 不再参与判断：即使 isAuthorized=null 的关系也可访问
     */
    @Test
    void listAccessiblePatientUserIds_nullAuthorization_notFiltered() {
        UserPatientRelation nullAuth = new UserPatientRelation();
        nullAuth.setPatientUserId(10L);
        nullAuth.setIsAuthorized(null);

        when(userPatientRelationMapper.selectList(any())).thenReturn(List.of(nullAuth));

        List<Long> result = userPatientRelationService.listAccessiblePatientUserIds(CURRENT_USER_ID);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0));
    }

    /**
     * 无关系记录时返回空列表
     */
    @Test
    void listAccessiblePatientUserIds_noRelations_returnsEmpty() {
        when(userPatientRelationMapper.selectList(any())).thenReturn(List.of());

        List<Long> result = userPatientRelationService.listAccessiblePatientUserIds(CURRENT_USER_ID);

        assertTrue(result.isEmpty());
    }

    // ===== 辅助构造 =====

    private UserPatientRelation buildRelation(Long patientUserId, Integer isAuthorized) {
        UserPatientRelation relation = new UserPatientRelation();
        relation.setPatientUserId(patientUserId);
        relation.setIsAuthorized(isAuthorized);
        return relation;
    }
}
