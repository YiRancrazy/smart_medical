package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.RegistrationMapper;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.AppointmentRuleService;
import com.yirancrazy.smartmedical.service.DegreeService;
import com.yirancrazy.smartmedical.service.DepartmentService;
import com.yirancrazy.smartmedical.service.DoctorPositionService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DoctorManager#callPatient 单测
 * 覆盖：叫号接诊的状态/医生匹配校验（B09 统一以 template.doctorId 校验）。
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生叫号业务单测
 * @Datetime: 2026-07-24 15:30
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class DoctorManagerTest {

    @Mock private DoctorService doctorService;
    @Mock private DepartmentService departmentService;
    @Mock private RegistrationScheduleService registrationScheduleService;
    @Mock private RegistrationScheduleTemplateService registrationScheduleTemplateService;
    @Mock private DoctorPositionService doctorPositionService;
    @Mock private AppointmentRuleService appointmentRuleService;
    @Mock private DegreeService degreeService;
    @Mock private RegistrationService registrationService;
    @Mock private RegistrationMapper registrationMapper;
    @Mock private RegistrationStatusLogManager statusLogManager;
    @Mock private UserService userService;
    @Mock private AccountService accountService;

    @InjectMocks
    private DoctorManager doctorManager;

    private static final Long REG_ID = 1001L;
    private static final Long DOCTOR_ID = 55L;
    private static final Long SCHEDULE_ID = 2001L;
    private static final Long TEMPLATE_ID = 3001L;

    /**
     * callPatient happy path：status=REPORTED + 医生匹配 → transition 到 IN_TREATMENT
     */
    @Test
    void callPatient_happyPath_transitionsToInTreatment() {
        Registration reg = buildRegistration(REG_ID, RegistrationStatusEnum.REPORTED.getCode(), SCHEDULE_ID);
        RegistrationSchedule schedule = buildSchedule(SCHEDULE_ID, TEMPLATE_ID);
        RegistrationScheduleTemplate template = buildTemplate(TEMPLATE_ID, DOCTOR_ID);

        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);
        when(registrationScheduleService.getRegistrationScheduleById(SCHEDULE_ID)).thenReturn(schedule);
        when(registrationScheduleTemplateService.getRegistrationScheduleTemplateById(TEMPLATE_ID)).thenReturn(template);

        doctorManager.callPatient(REG_ID, DOCTOR_ID);

        verify(statusLogManager).transition(eq(reg), eq(RegistrationStatusEnum.IN_TREATMENT.getCode()),
                eq(DOCTOR_ID), eq("doctor"), eq("叫号接诊"));
    }

    /**
     * callPatient 挂号记录不存在 → REGISTRATION_NOT_FOUND
     */
    @Test
    void callPatient_notFound_throws() {
        when(registrationService.getRegistrationById(REG_ID)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> doctorManager.callPatient(REG_ID, DOCTOR_ID));
        assertEquals(BizErrorCode.REGISTRATION_NOT_FOUND.getCode(), ex.getCode());
    }

    /**
     * callPatient 医生不匹配（template.doctorId != 当前医生）→ DOCTOR_NOT_MATCH
     */
    @Test
    void callPatient_doctorMismatch_throws() {
        Registration reg = buildRegistration(REG_ID, RegistrationStatusEnum.REPORTED.getCode(), SCHEDULE_ID);
        RegistrationSchedule schedule = buildSchedule(SCHEDULE_ID, TEMPLATE_ID);
        RegistrationScheduleTemplate template = buildTemplate(TEMPLATE_ID, 999L);

        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);
        when(registrationScheduleService.getRegistrationScheduleById(SCHEDULE_ID)).thenReturn(schedule);
        when(registrationScheduleTemplateService.getRegistrationScheduleTemplateById(TEMPLATE_ID)).thenReturn(template);

        BizException ex = assertThrows(BizException.class,
                () -> doctorManager.callPatient(REG_ID, DOCTOR_ID));
        assertEquals(BizErrorCode.DOCTOR_NOT_MATCH.getCode(), ex.getCode());
        verify(statusLogManager, never()).transition(any(), any(Integer.class), any(Long.class), any(), any());
    }

    /**
     * callPatient 状态非 REPORTED(5) → REGISTRATION_STATUS_INVALID
     */
    @Test
    void callPatient_wrongStatus_throws() {
        Registration reg = buildRegistration(REG_ID, RegistrationStatusEnum.SUCCESS.getCode(), SCHEDULE_ID);
        RegistrationSchedule schedule = buildSchedule(SCHEDULE_ID, TEMPLATE_ID);
        RegistrationScheduleTemplate template = buildTemplate(TEMPLATE_ID, DOCTOR_ID);

        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);
        when(registrationScheduleService.getRegistrationScheduleById(SCHEDULE_ID)).thenReturn(schedule);
        when(registrationScheduleTemplateService.getRegistrationScheduleTemplateById(TEMPLATE_ID)).thenReturn(template);

        BizException ex = assertThrows(BizException.class,
                () -> doctorManager.callPatient(REG_ID, DOCTOR_ID));
        assertEquals(BizErrorCode.REGISTRATION_STATUS_INVALID.getCode(), ex.getCode());
    }

    /**
     * callPatient 排班为 null → DOCTOR_NOT_MATCH
     */
    @Test
    void callPatient_nullSchedule_throws() {
        Registration reg = buildRegistration(REG_ID, RegistrationStatusEnum.REPORTED.getCode(), SCHEDULE_ID);
        when(registrationService.getRegistrationById(REG_ID)).thenReturn(reg);
        when(registrationScheduleService.getRegistrationScheduleById(SCHEDULE_ID)).thenReturn(null);

        BizException ex = assertThrows(BizException.class,
                () -> doctorManager.callPatient(REG_ID, DOCTOR_ID));
        assertEquals(BizErrorCode.DOCTOR_NOT_MATCH.getCode(), ex.getCode());
    }

    // ===== 辅助构造 =====

    private Registration buildRegistration(Long id, Integer status, Long scheduleId) {
        Registration reg = new Registration();
        reg.setId(id);
        reg.setStatus(status);
        reg.setRegistrationScheduleId(scheduleId);
        return reg;
    }

    private RegistrationSchedule buildSchedule(Long id, Long templateId) {
        RegistrationSchedule schedule = new RegistrationSchedule();
        schedule.setId(id);
        schedule.setRegistrationScheduleTemplateId(templateId);
        return schedule;
    }

    private RegistrationScheduleTemplate buildTemplate(Long id, Long doctorId) {
        RegistrationScheduleTemplate template = new RegistrationScheduleTemplate();
        template.setId(id);
        template.setDoctorId(doctorId);
        return template;
    }
}
