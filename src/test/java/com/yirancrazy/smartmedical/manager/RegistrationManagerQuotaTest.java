package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.mapper.RegistrationScheduleMapper;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.service.*;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * RegistrationManager 单测
 * 覆盖 BUG-B03: 号源不足时正确返回失败，配合 DDL CHECK 约束兜底
 */
@ExtendWith(MockitoExtension.class)
class RegistrationManagerQuotaTest {

    @InjectMocks
    private RegistrationManager registrationManager;

    @Mock
    private RegistrationService registrationService;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private PatientCardService patientCardService;
    @Mock
    private PatientService patientService;
    @Mock
    private PatientManager patientManager;
    @Mock
    private OrderService orderService;
    @Mock
    private OrderTypeService orderTypeService;
    @Mock
    private OrderItemService orderItemService;
    @Mock
    private RegistrationScheduleService registrationScheduleService;
    @Mock
    private RegistrationScheduleMapper registrationScheduleMapper;
    @Mock
    private RegistrationScheduleTemplateService registrationScheduleTemplateService;
    @Mock
    private DoctorService doctorService;
    @Mock
    private UserService userService;
    @Mock
    private DepartmentService departmentService;
    @Mock
    private DoctorPositionService doctorPositionService;
    @Mock
    private RegistrationStatusLogService registrationStatusLogService;

    @Test
    void addRegistration_whenNoQuota_shouldReturnFail() {
        Long scheduleId = 1L;
        Long patientCardId = 2L;
        Long userId = 3L;

        PatientCard patientCard = new PatientCard();
        patientCard.setId(patientCardId);

        Patient patient = new Patient();
        patient.setUserId(userId);

        RegistrationSchedule schedule = new RegistrationSchedule();
        schedule.setId(scheduleId);
        schedule.setRegistrationScheduleTemplateId(4L);

        when(patientCardService.getPatientCardById(patientCardId)).thenReturn(patientCard);
        when(patientService.getPatientByPatientCardId(patientCardId)).thenReturn(patient);
        when(registrationScheduleService.getRegistrationScheduleById(scheduleId)).thenReturn(schedule);
        when(registrationScheduleMapper.update(any(), any())).thenReturn(0);

        Result<String> result = registrationManager.addRegistration(scheduleId, userId, patientCardId);

        assertEquals(500, result.getCode());
        assertEquals("该排班已无号源", result.getMessage());
        verify(registrationService, never()).insertRegistration(any());
    }
}
