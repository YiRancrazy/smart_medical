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
 * UserPatientRelationServiceImpl 患者关系查询单测
 * 覆盖：patient_id 语义、关系存在即访问与去重。
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
     * 返回所有已添加患者的 patientId，并去重
     */
    @Test
    void listAccessiblePatientIds_returnsDistinctPatientIds() {
        UserPatientRelation self = buildRelation(7L);
        UserPatientRelation other = buildRelation(8L);
        UserPatientRelation duplicate = buildRelation(8L);

        when(userPatientRelationMapper.selectList(any())).thenReturn(List.of(self, other, duplicate));

        List<Long> result = userPatientRelationService.listAccessiblePatientIds(CURRENT_USER_ID);

        assertEquals(List.of(7L, 8L), result);
    }

    /**
     * 关系表不再有授权状态，存在关系即可访问
     */
    @Test
    void listAccessiblePatientIds_relationExists_returnsPatient() {
        UserPatientRelation relation = buildRelation(10L);

        when(userPatientRelationMapper.selectList(any())).thenReturn(List.of(relation));

        List<Long> result = userPatientRelationService.listAccessiblePatientIds(CURRENT_USER_ID);

        assertEquals(1, result.size());
        assertEquals(10L, result.get(0));
    }

    /**
     * 无关系记录时返回空列表
     */
    @Test
    void listAccessiblePatientIds_noRelations_returnsEmpty() {
        when(userPatientRelationMapper.selectList(any())).thenReturn(List.of());

        List<Long> result = userPatientRelationService.listAccessiblePatientIds(CURRENT_USER_ID);

        assertTrue(result.isEmpty());
    }

    /**
     * 关系存在性按 patient_id 查询
     */
    @Test
    void hasPatientRelation_queriesPatientId() {
        when(userPatientRelationMapper.selectCount(any())).thenReturn(1L);

        assertTrue(userPatientRelationService.hasPatientRelation(CURRENT_USER_ID, 20L));
    }

    // ===== 辅助构造 =====

    private UserPatientRelation buildRelation(Long patientId) {
        UserPatientRelation relation = new UserPatientRelation();
        relation.setPatientId(patientId);
        return relation;
    }
}
