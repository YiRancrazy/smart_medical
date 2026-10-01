package com.yirancrazy.smartmedical.service.impl;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.MedicalRecordMapper;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 病历 Service 查询语义单测
 * @Author: YiRanCrazy@gmail.com
 * @Description: 覆盖空集合短路与分页查询语义
 * @Datetime: 2026-10-01 02:00
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceImplTest {

    @Mock
    private MedicalRecordMapper medicalRecordMapper;

    @InjectMocks
    private MedicalRecordServiceImpl medicalRecordService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(medicalRecordService, "baseMapper", medicalRecordMapper);
    }

    @Test
    void listByRegistrationIds_empty_returnsEmptyWithoutQuery() {
        assertTrue(medicalRecordService.listByRegistrationIds(Collections.emptyList()).isEmpty());
        verifyNoInteractions(medicalRecordMapper);
    }

    @Test
    void listByPatientUserIds_empty_returnsEmptyWithoutQuery() {
        assertTrue(medicalRecordService.listByPatientUserIds(Collections.emptyList()).isEmpty());
        verifyNoInteractions(medicalRecordMapper);
    }

    @Test
    void listByPatientUserIds_nonEmpty_queriesMapper() {
        MedicalRecord record = new MedicalRecord();
        record.setId(1L);
        when(medicalRecordMapper.selectList(any())).thenReturn(List.of(record));

        List<MedicalRecord> result = medicalRecordService.listByPatientUserIds(List.of(100L));

        assertEquals(List.of(record), result);
        verify(medicalRecordMapper).selectList(any());
    }

    @Test
    void listMedicalRecords_page_emptyPatientIds_returnsEmptyPageWithoutQuery() {
        PageInfo<MedicalRecord> result = medicalRecordService
                .listMedicalRecordsByPatientUserIdsAndDoctorIdPage(
                        Collections.emptyList(), 10L, null, null, 1, 10);

        assertTrue(result.getList().isEmpty());
        verifyNoInteractions(medicalRecordMapper);
    }
}
