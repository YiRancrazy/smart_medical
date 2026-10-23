package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.yirancrazy.smartmedical.mapper.PatientMapper;
import com.yirancrazy.smartmedical.pojo.Patient;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 患者 Service 身份查询单测
 */
@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientServiceImpl patientService;

    @BeforeEach
    void setUpTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), Patient.class);
    }

    @Test
    void getPatientByIdCard_returnsPatient() {
        Patient patient = new Patient();
        patient.setId(10L);
        patient.setIdCard("11010519491231002X");
        when(patientMapper.selectOne(any())).thenReturn(patient);

        assertSame(patient, patientService.getPatientByIdCard("11010519491231002X"));
    }

    @Test
    void getPatientByOwnerUserId_returnsPatient() {
        Patient patient = new Patient();
        patient.setId(10L);
        patient.setOwnerUserId(99L);
        when(patientMapper.selectOne(any())).thenReturn(patient);

        assertSame(patient, patientService.getPatientByOwnerUserId(99L));
    }

    @Test
    void getPatientByIdCardForUpdate_returnsLockingQueryResult() {
        Patient patient = new Patient();
        patient.setId(10L);
        patient.setIdCard("11010519491231002X");
        when(patientMapper.selectOne(any())).thenReturn(patient);

        assertSame(patient, patientService.getPatientByIdCardForUpdate("11010519491231002X"));
    }
}
