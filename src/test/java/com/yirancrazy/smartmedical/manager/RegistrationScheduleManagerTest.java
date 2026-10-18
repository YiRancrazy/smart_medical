package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.constant.status.AppointmentRuleStatusEnum;
import com.yirancrazy.smartmedical.constant.status.AppointmentRuleTypeEnum;
import com.yirancrazy.smartmedical.constant.status.RegistrationScheduleStatusEnum;
import com.yirancrazy.smartmedical.pojo.AppointmentRule;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.vo.registration.confirm.RegistrationDateAndRemainQuotaVo;
import com.yirancrazy.smartmedical.service.AppointmentRuleService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * RegistrationScheduleManager 排班查询单测
 * 覆盖：医生 / 科室 / 全院默认规则的解析顺序与回退（对齐挂号医生列表接口口径）。
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班规则回退单测
 * @Datetime: 2026-10-03 19:10
 * @Version: 1.0
 */
@ExtendWith(MockitoExtension.class)
class RegistrationScheduleManagerTest {

    private static final Long DOCTOR_ID = 2106273597932777472L;
    private static final Long DEPARTMENT_ID = 2030112388347453441L;
    private static final Long TEMPLATE_ID = 2106293198712299520L;
    private static final Long SCHEDULE_ID = 2106293198867488768L;

    @Mock private RegistrationScheduleService registrationScheduleService;
    @Mock private AppointmentRuleService appointmentRuleService;
    @Mock private DoctorService doctorService;
    @Mock private RegistrationScheduleTemplateService registrationScheduleTemplateService;

    @InjectMocks
    private RegistrationScheduleManager registrationScheduleManager;

    /**
     * 医生 / 科室均无规则时回退到全院默认规则，排班不再报「没有可用的挂号配置」
     */
    @Test
    void listRegistrationsByDoctorIdAndMaxAdvanceDays_fallsBackToGlobalRule() {
        Long doctorId = 100L;
        Long departmentId = 900L;
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));

        when(doctorService.getDoctorById(doctorId)).thenReturn(doctor(doctorId, departmentId));
        when(appointmentRuleService.listAppointmentsRulesByDoctorId(doctorId)).thenReturn(List.of());
        when(appointmentRuleService.listAppointmentsRulesByDepartmentId(departmentId)).thenReturn(List.of());
        when(appointmentRuleService.listGlobalAppointmentRules()).thenReturn(List.of(globalOutPatientRule(7)));
        when(registrationScheduleTemplateService.listRegistrationScheduleTemplatesByDoctorId(doctorId))
                .thenReturn(List.of(template(TEMPLATE_ID, doctorId, today)));
        when(registrationScheduleService.listRegistrationScheduleByRegistrationScheduleIdList(anyList()))
                .thenReturn(List.of(schedule(SCHEDULE_ID, TEMPLATE_ID, 10)));

        Result<List<RegistrationDateAndRemainQuotaVo>> result =
                registrationScheduleManager.listRegistrationsByDoctorIdAndMaxAdvanceDays(doctorId);

        assertEquals(200, result.getCode());
        assertEquals(1, result.getData().size());
        RegistrationDateAndRemainQuotaVo vo = result.getData().get(0);
        assertEquals(today, vo.getDate());
        assertEquals(10, vo.getRemainQuota());
    }

    /**
     * 医生专属规则存在时优先使用，不再查询科室 / 全院规则
     */
    @Test
    void listRegistrationsByDoctorIdAndMaxAdvanceDays_doctorRulePreemptsFallback() {
        Long doctorId = 100L;
        Long departmentId = 900L;
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));

        when(doctorService.getDoctorById(doctorId)).thenReturn(doctor(doctorId, departmentId));
        when(appointmentRuleService.listAppointmentsRulesByDoctorId(doctorId))
                .thenReturn(List.of(globalOutPatientRule(7)));
        when(registrationScheduleTemplateService.listRegistrationScheduleTemplatesByDoctorId(doctorId))
                .thenReturn(List.of(template(TEMPLATE_ID, doctorId, today)));
        when(registrationScheduleService.listRegistrationScheduleByRegistrationScheduleIdList(anyList()))
                .thenReturn(List.of(schedule(SCHEDULE_ID, TEMPLATE_ID, 10)));

        Result<List<RegistrationDateAndRemainQuotaVo>> result =
                registrationScheduleManager.listRegistrationsByDoctorIdAndMaxAdvanceDays(doctorId);

        assertEquals(200, result.getCode());
        verify(appointmentRuleService, never()).listAppointmentsRulesByDepartmentId(eq(departmentId));
        verify(appointmentRuleService, never()).listGlobalAppointmentRules();
    }

    /**
     * 三级规则均无可用门诊规则时，仍返回失败（保持原有语义）
     */
    @Test
    void listRegistrationsByDoctorIdAndMaxAdvanceDays_noOutPatientRule_returnsFail() {
        Long doctorId = 100L;
        Long departmentId = 900L;

        when(doctorService.getDoctorById(doctorId)).thenReturn(doctor(doctorId, departmentId));
        when(appointmentRuleService.listAppointmentsRulesByDoctorId(doctorId)).thenReturn(List.of());
        when(appointmentRuleService.listAppointmentsRulesByDepartmentId(departmentId)).thenReturn(List.of());
        AppointmentRule emergencyRule = globalOutPatientRule(7);
        emergencyRule.setRuleType(AppointmentRuleTypeEnum.EMERGENCY.getCode());
        when(appointmentRuleService.listGlobalAppointmentRules()).thenReturn(List.of(emergencyRule));

        Result<List<RegistrationDateAndRemainQuotaVo>> result =
                registrationScheduleManager.listRegistrationsByDoctorIdAndMaxAdvanceDays(doctorId);

        assertEquals(500, result.getCode());
        assertEquals("没有可用的挂号配置", result.getMessage());
    }

    private Doctor doctor(Long doctorId, Long departmentId) {
        Doctor doctor = new Doctor();
        doctor.setId(doctorId);
        doctor.setDepartmentId(departmentId);
        return doctor;
    }

    private AppointmentRule globalOutPatientRule(int maxAdvanceDays) {
        AppointmentRule rule = new AppointmentRule();
        rule.setStatus(AppointmentRuleStatusEnum.NORMAL.getCode());
        rule.setRuleType(AppointmentRuleTypeEnum.OUT_PATIENT.getCode());
        rule.setPriority(2);
        rule.setMaxAdvanceDays(maxAdvanceDays);
        return rule;
    }

    private RegistrationScheduleTemplate template(Long id, Long doctorId, LocalDate date) {
        RegistrationScheduleTemplate template = new RegistrationScheduleTemplate();
        template.setId(id);
        template.setDoctorId(doctorId);
        template.setRegistrationDate(date);
        template.setEnabled(true);
        template.setTotalQuota(30);
        return template;
    }

    private RegistrationSchedule schedule(Long id, Long templateId, int remainingQuota) {
        RegistrationSchedule schedule = new RegistrationSchedule();
        schedule.setId(id);
        schedule.setRegistrationScheduleTemplateId(templateId);
        schedule.setStatus(RegistrationScheduleStatusEnum.NORMAL.getCode());
        schedule.setRemainingQuota(remainingQuota);
        return schedule;
    }
}
