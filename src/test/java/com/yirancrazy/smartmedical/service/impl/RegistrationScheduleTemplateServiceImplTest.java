package com.yirancrazy.smartmedical.service.impl;

import com.yirancrazy.smartmedical.mapper.RegistrationScheduleTemplateMapper;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * RegistrationScheduleTemplateServiceImpl 单测
 * 覆盖：按医生 + 日期查询模板时空参判空，不再回退查全表
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班模板服务判空与查询语义测试
 * @Datetime: 2026-09-09 10:00
 * @Version: 1.0
 */

@ExtendWith(MockitoExtension.class)
class RegistrationScheduleTemplateServiceImplTest {

    @InjectMocks
    private RegistrationScheduleTemplateServiceImpl registrationScheduleTemplateService;

    @Mock
    private RegistrationScheduleTemplateMapper registrationScheduleTemplateMapper;

    @Test
    void getByDoctorIdAndDate_whenDateNull_shouldReturnEmptyWithoutQuery() {
        List<RegistrationScheduleTemplate> result =
                registrationScheduleTemplateService.getRegistrationScheduleTemplateByDoctorIdAndDate(1L, null);

        assertTrue(result.isEmpty());
        verifyNoInteractions(registrationScheduleTemplateMapper);
    }

    @Test
    void getByDoctorIdAndDate_whenDoctorIdNull_shouldReturnEmptyWithoutQuery() {
        List<RegistrationScheduleTemplate> result =
                registrationScheduleTemplateService.getRegistrationScheduleTemplateByDoctorIdAndDate(null, LocalDate.of(2026, 9, 9));

        assertTrue(result.isEmpty());
        verifyNoInteractions(registrationScheduleTemplateMapper);
    }

    @Test
    void getByDoctorIdAndDate_whenParamsValid_shouldQueryMapper() {
        RegistrationScheduleTemplate template = new RegistrationScheduleTemplate();
        template.setId(1L);
        when(registrationScheduleTemplateMapper.selectList(any())).thenReturn(Collections.singletonList(template));

        List<RegistrationScheduleTemplate> result =
                registrationScheduleTemplateService.getRegistrationScheduleTemplateByDoctorIdAndDate(1L, LocalDate.of(2026, 9, 9));

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
