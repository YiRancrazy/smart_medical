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
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班Manager层
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Slf4j
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
        if (doctor == null) {
            return Result.fail("医生不存在");
        }

        List<AppointmentRule> appointmentRules = new ArrayList<>();

        List<AppointmentRule> currentDoctorAppointmentRules = appointmentRuleService.listAppointmentsRulesByDoctorId(doctorId);
        if (currentDoctorAppointmentRules.isEmpty()) {
            // ponytail: S13 — 无医生规则时回落到科室规则，不再跨科室兜底取全院规则
            appointmentRules = appointmentRuleService.listAppointmentsRulesByDepartmentId(doctor.getDepartmentId());
        } else {
            appointmentRules = currentDoctorAppointmentRules;
        }

        appointmentRules = appointmentRules
                .stream()
                .filter(item -> Objects.equals(item.getStatus(), AppointmentRuleStatusEnum.NORMAL.getCode()))
                .filter(item -> Objects.equals(item.getRuleType(), AppointmentRuleTypeEnum.OUT_PATIENT.getCode()))
                .sorted(Comparator.comparing(AppointmentRule::getPriority, Comparator.nullsLast(Integer::compare)))
                .toList();

        if (appointmentRules.isEmpty()) {
            return Result.fail("没有可用的挂号配置");
        }
        AppointmentRule currentAppointmentRule = appointmentRules.get(0);

        List<RegistrationScheduleTemplate> registrationScheduleTemplateList = registrationScheduleTemplateService.listRegistrationScheduleTemplatesByDoctorId(doctorId);

        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(currentAppointmentRule.getMaxAdvanceDays());
        registrationScheduleTemplateList = registrationScheduleTemplateList.stream()
                .filter(item -> !item.getRegistrationDate().isBefore(today) &&
                        !item.getRegistrationDate().isAfter(maxDate)).toList();

        if (registrationScheduleTemplateList.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<Long> registrationScheduleIdList = registrationScheduleTemplateList.stream().map(RegistrationScheduleTemplate::getId).toList();

        List<RegistrationSchedule> registrationSchedules = registrationScheduleService
                .listRegistrationScheduleByRegistrationScheduleIdList(registrationScheduleIdList);

        // ponytail: S14 — 按 date 聚合同日所有 schedule 的剩余/总号源，避免跳过同日上午+下午
        java.util.Map<LocalDate, RegistrationDateAndRemainQuotaVo> mergedByDate = new java.util.TreeMap<>();
        for (RegistrationScheduleTemplate registrationScheduleTemplate : registrationScheduleTemplateList) {
            RegistrationSchedule registrationSchedule = registrationSchedules.stream()
                    .filter(item -> Objects.equals(item.getRegistrationScheduleTemplateId(), registrationScheduleTemplate.getId()))
                    .findFirst().orElse(null);
            if (registrationSchedule == null) {
                log.warn("跳过排班模板 {}：未找到对应排班记录", registrationScheduleTemplate.getId());
                continue;
            }
            LocalDate date = registrationScheduleTemplate.getRegistrationDate();
            RegistrationDateAndRemainQuotaVo vo = mergedByDate.get(date);
            if (vo == null) {
                vo = new RegistrationDateAndRemainQuotaVo();
                vo.setDoctorId(String.valueOf(registrationSchedule.getDoctorId()));
                vo.setDate(date);
                vo.setTotalQuota(0);
                vo.setRemainQuota(0);
                mergedByDate.put(date, vo);
            }
            vo.setTotalQuota(vo.getTotalQuota() + (registrationScheduleTemplate.getTotalQuota() == null ? 0 : registrationScheduleTemplate.getTotalQuota()));
            vo.setRemainQuota(vo.getRemainQuota() + (registrationSchedule.getRemainingQuota() == null ? 0 : registrationSchedule.getRemainingQuota()));
        }

        List<RegistrationDateAndRemainQuotaVo> result = new ArrayList<>(mergedByDate.values());
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
        // 仅保留已启用模板
        List<Long> registrationScheduleIdList = registrationScheduleTemplates
                .stream()
                .filter(t -> Boolean.TRUE.equals(t.getEnabled()))
                .map(RegistrationScheduleTemplate::getId)
                .toList();

        List<RegistrationSchedule> registrationSchedulesByDoctorIdAndDate = registrationScheduleService
                .getRegistrationScheduleListByRegistrationScheduleIdList(registrationScheduleIdList);

        List<RegistrationConfirmTime> registrationConfirmTimeList = new CopyOnWriteArrayList<>();
        for (RegistrationSchedule registrationSchedule : registrationSchedulesByDoctorIdAndDate) {
            // 仅展示正常(1)状态的排班
            if (registrationSchedule.getStatus() == null || registrationSchedule.getStatus() != 1) {
                continue;
            }
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
        // 数据库存储已为"分"，直接返回
        Integer priceInFen = registrationScheduleTemplate.getPrice();
        return Result.success(priceInFen);
    }
}
