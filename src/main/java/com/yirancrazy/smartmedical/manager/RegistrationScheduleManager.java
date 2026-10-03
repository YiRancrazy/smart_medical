package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.status.AppointmentRuleStatusEnum;
import com.yirancrazy.smartmedical.constant.status.AppointmentRuleTypeEnum;
import com.yirancrazy.smartmedical.constant.status.RegistrationScheduleStatusEnum;
import com.yirancrazy.smartmedical.pojo.AppointmentRule;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.vo.registration.confirm.RegistrationConfirmTime;
import com.yirancrazy.smartmedical.pojo.vo.registration.confirm.RegistrationDateAndRemainQuotaVo;
import com.yirancrazy.smartmedical.service.AppointmentRuleService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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

        // 规则解析口径与 DoctorManager#getRegistrationDoctorBaseInfoByDepartmentId 对齐：
        // 医生专属 → 科室 → 全院默认（department_id/doctor_id 均为 NULL），且不跨科室套用他科规则；
        // 否则新科室未单独配置规则时，科室页能列出医生、排班页却直接报「没有可用的挂号配置」
        List<AppointmentRule> appointmentRules = resolveAppointmentRules(doctor)
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

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Shanghai"));
        // M17: maxAdvanceDays 可能为 null，null 时兜底为 0（仅当天），避免 NPE
        Integer maxAdvanceDays = currentAppointmentRule.getMaxAdvanceDays();
        LocalDate maxDate = today.plusDays(maxAdvanceDays == null ? 0 : maxAdvanceDays);
        registrationScheduleTemplateList = registrationScheduleTemplateList.stream()
                .filter(item -> !item.getRegistrationDate().isBefore(today) &&
                        !item.getRegistrationDate().isAfter(maxDate)).toList();

        if (registrationScheduleTemplateList.isEmpty()) {
            return Result.success(new ArrayList<>());
        }
        List<Long> registrationScheduleIdList = registrationScheduleTemplateList.stream().map(RegistrationScheduleTemplate::getId).toList();

        // 与时段接口同口径：仅聚合已启用模板与正常排班，否则日期余量含停诊/禁用号源导致选中后对不上
        registrationScheduleTemplateList = registrationScheduleTemplateList.stream()
                .filter(t -> Boolean.TRUE.equals(t.getEnabled()))
                .toList();

        List<RegistrationSchedule> registrationSchedules = registrationScheduleService
                .listRegistrationScheduleByRegistrationScheduleIdList(registrationScheduleIdList);

        // ponytail: S14 — 按 date 聚合同日所有 schedule 的剩余/总号源，避免跳过同日上午+下午
        // 一个模板按小时生成多条 schedule（见 ExcelManager.buildSchedules），须累加全部正常排班余量，
        // findFirst 只算第一个小时且首条停诊会误跳过整个模板
        java.util.Map<LocalDate, RegistrationDateAndRemainQuotaVo> mergedByDate = new java.util.TreeMap<>();
        for (RegistrationScheduleTemplate registrationScheduleTemplate : registrationScheduleTemplateList) {
            int templateRemaining = 0;
            boolean hasNormalSchedule = false;
            for (RegistrationSchedule registrationSchedule : registrationSchedules) {
                if (!Objects.equals(registrationSchedule.getRegistrationScheduleTemplateId(), registrationScheduleTemplate.getId())) {
                    continue;
                }
                if (registrationSchedule.getStatus() == null
                        || !RegistrationScheduleStatusEnum.NORMAL.getCode().equals(registrationSchedule.getStatus())) {
                    continue;
                }
                hasNormalSchedule = true;
                templateRemaining += (registrationSchedule.getRemainingQuota() == null ? 0 : registrationSchedule.getRemainingQuota());
            }
            if (!hasNormalSchedule) {
                continue;
            }
            LocalDate date = registrationScheduleTemplate.getRegistrationDate();
            RegistrationDateAndRemainQuotaVo vo = mergedByDate.get(date);
            if (vo == null) {
                vo = new RegistrationDateAndRemainQuotaVo();
                vo.setDoctorId(String.valueOf(registrationScheduleTemplate.getDoctorId()));
                vo.setDate(date);
                vo.setTotalQuota(0);
                vo.setRemainQuota(0);
                mergedByDate.put(date, vo);
            }
            vo.setTotalQuota(vo.getTotalQuota() + (registrationScheduleTemplate.getTotalQuota() == null ? 0 : registrationScheduleTemplate.getTotalQuota()));
            vo.setRemainQuota(vo.getRemainQuota() + templateRemaining);
        }

        List<RegistrationDateAndRemainQuotaVo> result = new ArrayList<>(mergedByDate.values());
        result.sort(Comparator.comparing(RegistrationDateAndRemainQuotaVo::getDate));

        return Result.success(result);
    }

    /**
     * 解析医生可用的挂号规则：医生专属规则 → 科室规则 → 全院默认规则
     * @param doctor 医生实体
     * @return 待进一步按状态 / 规则类型过滤的规则列表
     */
    private List<AppointmentRule> resolveAppointmentRules(Doctor doctor) {
        List<AppointmentRule> doctorRules = appointmentRuleService.listAppointmentsRulesByDoctorId(doctor.getId());
        if (!doctorRules.isEmpty()) {
            return doctorRules;
        }
        List<AppointmentRule> departmentRules =
                appointmentRuleService.listAppointmentsRulesByDepartmentId(doctor.getDepartmentId());
        if (!departmentRules.isEmpty()) {
            return departmentRules;
        }
        return appointmentRuleService.listGlobalAppointmentRules();
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

        List<RegistrationConfirmTime> registrationConfirmTimeList = new ArrayList<>();
        for (RegistrationSchedule registrationSchedule : registrationSchedulesByDoctorIdAndDate) {
            // 仅展示正常(1)状态的排班
            if (registrationSchedule.getStatus() == null
                    || !RegistrationScheduleStatusEnum.NORMAL.getCode().equals(registrationSchedule.getStatus())) {
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

    /**
     * 按排班 ID 查询挂号价格（数据库以「分」存储，直接返回分，由前端格式化）
     * @param registrationScheduleId 排班 ID
     * @return 价格（分）；排班或模板不存在返回失败
     */
    public Result<Integer> getRegistrationPriceByRegistrationScheduleId(Long registrationScheduleId) {
        RegistrationSchedule registrationSchedule = registrationScheduleService
                .getRegistrationScheduleById(registrationScheduleId);
        if (registrationSchedule == null) {
            return Result.fail("挂号排班不存在");
        }
        RegistrationScheduleTemplate registrationScheduleTemplate = registrationScheduleTemplateService
                .getRegistrationScheduleTemplateById(registrationSchedule.getRegistrationScheduleTemplateId());
        if (registrationScheduleTemplate == null) {
            return Result.fail("挂号排班模板不存在");
        }
        // 数据库存储为"分"，直接返回
        Integer priceInFen = registrationScheduleTemplate.getPrice();
        return Result.success(priceInFen);
    }
}
