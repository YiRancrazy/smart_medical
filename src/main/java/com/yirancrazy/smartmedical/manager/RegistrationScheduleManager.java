package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.status.AppointmentRuleStatusEnum;
import com.yirancrazy.smartmedical.constant.status.AppointmentRuleTypeEnum;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.vo.registration.confirm.RegistrationConfirmTime;
import com.yirancrazy.smartmedical.pojo.vo.registration.confirm.RegistrationDateAndRemainQuotaVo;
import com.yirancrazy.smartmedical.service.AppointmentRuleService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班Manager层
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class RegistrationScheduleManager {

    private final RegistrationScheduleService registrationScheduleService;
    private final AppointmentRuleService appointmentRuleService;
    private final DoctorService doctorService;
    private final RegistrationScheduleTemplateService registrationScheduleTemplateService;

    /**
     * 获取医生挂号可预约日期内排班信息
     * @param doctorId 医生id
     * @return 挂号排班信息
     */
    public Result<List<RegistrationDateAndRemainQuotaVo>> listRegistrationsByDoctorIdAndMaxAdvanceDays(Long doctorId) {

        Doctor doctor = doctorService.getDoctorById(doctorId);

        List<AppointmentRule> appointmentRules = new ArrayList<>();

        List<AppointmentRule> currentDoctorAppointmentRules = appointmentRuleService.listAppointmentsRulesByDoctorId(doctorId);
        if (currentDoctorAppointmentRules.isEmpty()) {
            List<AppointmentRule> currentDepartmentAppointmentRules = appointmentRuleService.listAppointmentsRulesByDepartmentId(doctor.getDepartmentId());
            if (currentDepartmentAppointmentRules.isEmpty()) {
                currentDoctorAppointmentRules = appointmentRuleService.listAllAppointmentRules();
                appointmentRules = currentDoctorAppointmentRules;
            } else {
                appointmentRules = currentDepartmentAppointmentRules;
            }
        } else {
            appointmentRules = currentDoctorAppointmentRules;
        }

        appointmentRules = appointmentRules
                .stream()
                .filter(item -> Objects.equals(item.getStatus(), AppointmentRuleStatusEnum.NORMAL.getCode()))
                .filter(item -> Objects.equals(item.getRuleType(), AppointmentRuleTypeEnum.OUT_PATIENT.getCode()))
                .sorted(new Comparator<AppointmentRule>() {
                    @Override
                    public int compare(AppointmentRule o1, AppointmentRule o2) {
                        return o1.getPriority() - o2.getPriority();
                    }
                })
                .toList();

        if (appointmentRules.isEmpty()) {
            return Result.fail("没有可用的挂号配置");
        }
        AppointmentRule currentAppointmentRule = appointmentRules.get(0);

        List<RegistrationScheduleTemplate> registrationScheduleTemplateList = registrationScheduleTemplateService.listRegistrationScheduleTemplatesByDoctorId(doctorId);

        registrationScheduleTemplateList = registrationScheduleTemplateList.stream()
                .filter(item -> item.getRegistrationDate().isAfter(LocalDate.now()) &&
                        item.getRegistrationDate().isBefore(LocalDate.now()
                                .plusDays(currentAppointmentRule.getMaxAdvanceDays()))).toList();

        if (registrationScheduleTemplateList.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<Long> registrationScheduleTemplateIdList = registrationScheduleTemplateList.stream().map(RegistrationScheduleTemplate::getId).toList();

        List<RegistrationSchedule> registrationSchedules = registrationScheduleService
                .listRegistrationScheduleByRegistrationScheduleTemplateIdList(registrationScheduleTemplateIdList);

        List<RegistrationDateAndRemainQuotaVo> registrationDateAndRemainQuotaVoList = new ArrayList<>();

        for (RegistrationScheduleTemplate registrationScheduleTemplate : registrationScheduleTemplateList) {
            RegistrationDateAndRemainQuotaVo registrationDateAndRemainQuotaVo = new RegistrationDateAndRemainQuotaVo();
            RegistrationSchedule registrationSchedule = registrationSchedules.stream()
                    .filter(item -> Objects.equals(item.getRegistrationScheduleTemplateId(), registrationScheduleTemplate.getId()))
                    .findFirst().orElse(null);
            registrationDateAndRemainQuotaVo.setDoctorId(String.valueOf(registrationSchedule.getDoctorId()));
            registrationDateAndRemainQuotaVo.setDate(registrationScheduleTemplate.getRegistrationDate());
            registrationDateAndRemainQuotaVo.setTotalQuota(registrationScheduleTemplate.getTotalQuota());
            registrationDateAndRemainQuotaVo.setRemainQuota(registrationScheduleTemplate.getTotalQuota() - registrationSchedules.stream()
                    .filter(item -> Objects.equals(item.getRegistrationScheduleTemplateId(), registrationScheduleTemplate.getId()))
                    .mapToInt(RegistrationSchedule::getRemainingQuota)
                    .sum());
            registrationDateAndRemainQuotaVoList.add(registrationDateAndRemainQuotaVo);
        }

        List<RegistrationDateAndRemainQuotaVo> result = new ArrayList<>();
        List<LocalDate> dateList = new ArrayList<>();
        for (RegistrationDateAndRemainQuotaVo registrationDateAndRemainQuotaVo : registrationDateAndRemainQuotaVoList) {

            if (dateList.contains(registrationDateAndRemainQuotaVo.getDate())) {
                continue;
            } else {
                dateList.add(registrationDateAndRemainQuotaVo.getDate());
                result.add(registrationDateAndRemainQuotaVo);
            }
        }

        result.sort(Comparator.comparing(RegistrationDateAndRemainQuotaVo::getDate));

        return Result.success(result);
    }

    /**
     * 获取医生可预约的挂号时间段
     * @param doctorId 医生id
     * @param date 挂号日期
     * @return 挂号时间段
     */
    public Result<List<RegistrationConfirmTime>> getRegistrationScheduleByDoctorIdAndDate(Long doctorId, LocalDate date) {
        List<RegistrationScheduleTemplate> registrationScheduleTemplates = registrationScheduleTemplateService.getRegistrationScheduleTemplateByDoctorIdAndDate(doctorId, date);
        List<Long> registrationScheduleTemplateIdList = registrationScheduleTemplates
                .stream()
                .map(RegistrationScheduleTemplate::getId)
                .toList();

        List<RegistrationSchedule> registrationSchedulesByDoctorIdAndDate = registrationScheduleService
                .getRegistrationScheduleListByRegistrationScheduleTemplateIdList(registrationScheduleTemplateIdList);

        List<RegistrationConfirmTime> registrationConfirmTimeList = new CopyOnWriteArrayList<>();
        for (RegistrationSchedule registrationSchedule : registrationSchedulesByDoctorIdAndDate) {
            RegistrationConfirmTime registrationConfirmTime = new RegistrationConfirmTime();
            registrationConfirmTime.setRegistrationScheduleId(String.valueOf(registrationSchedule.getId()));
            registrationConfirmTime.setStartTime(registrationSchedule.getStartTime());
            registrationConfirmTime.setEndTime(registrationSchedule.getEndTime());
            registrationConfirmTime.setAvailable(registrationSchedule.getRemainingQuota() > 0);
            registrationConfirmTime.setRemainQuota(registrationSchedule.getRemainingQuota());
            registrationConfirmTimeList.add(registrationConfirmTime);
        }
        return Result.success(registrationConfirmTimeList);
    }

    public Result<Integer> getRegistrationPriceByRegistrationScheduleId(String registrationScheduleId) {
        RegistrationSchedule registrationSchedule = registrationScheduleService
                .getRegistrationScheduleById(Long.valueOf(registrationScheduleId));
        if (registrationSchedule == null) {
            return Result.fail("挂号排班不存在");
        }
        RegistrationScheduleTemplate registrationScheduleTemplate = registrationScheduleTemplateService
                .getRegistrationScheduleTemplateById(registrationSchedule.getRegistrationScheduleTemplateId());
        if (registrationScheduleTemplate == null) {
            return Result.fail("挂号排班模板不存在");
        }
        return Result.success(registrationScheduleTemplate.getPrice());
    }
}
