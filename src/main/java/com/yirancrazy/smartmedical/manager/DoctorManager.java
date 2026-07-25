package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.constant.status.AppointmentRuleStatusEnum;
import com.yirancrazy.smartmedical.constant.status.AppointmentRuleTypeEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.RegistrationMapper;
import com.yirancrazy.smartmedical.pojo.*;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.DoctorScheduleVO;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.WaitingPatientVO;
import com.yirancrazy.smartmedical.pojo.dto.user.response.AdminDoctorSimpleResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.response.admin.detail.AdminDoctorDetailResponse;
import com.yirancrazy.smartmedical.pojo.dto.user.result.PageResult;
import com.yirancrazy.smartmedical.pojo.vo.DoctorVo;
import com.yirancrazy.smartmedical.pojo.vo.RegistrationDoctorBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.RegistrationDoctorConfirmVo;
import com.yirancrazy.smartmedical.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class DoctorManager {

    private final DoctorService doctorService;
    private final DepartmentService departmentService;
    private final RegistrationScheduleService registrationScheduleService;
    private final RegistrationScheduleTemplateService registrationScheduleTemplateService;
    private final DoctorPositionService doctorPositionService;
    private final AppointmentRuleService appointmentRuleService;
    private final DegreeService degreeService;
    private final RegistrationService registrationService;
    private final RegistrationMapper registrationMapper;
    private final RegistrationStatusLogManager statusLogManager;
    private final UserService userService;
    private final AccountService accountService;
    public int addDoctor(Doctor doctor) {
        doctor.setId(IdUtil.getSnowflakeNextId());
        return doctorService.insertDoctor(doctor);
    }

    public Result<DoctorVo> getDoctorById(Long id) {
        Doctor doctor = doctorService.getDoctorById(id);

        if (doctor == null) {
            return Result.info(404,"医生不存在",null);
        }

        Department department = doctor.getDepartmentId() == null ? null : departmentService.getDepartmentById(doctor.getDepartmentId());
        DoctorPosition doctorPosition = doctor.getDoctorPositionId() == null ? null : doctorPositionService.getPositionById(doctor.getDoctorPositionId());
        Degree degree = doctor.getDegreeId() == null ? null : degreeService.getDegreeById(doctor.getDegreeId());

        DoctorVo doctorVo = new DoctorVo();
        doctorVo.setDoctorId(String.valueOf(doctor.getId()));
        doctorVo.setDepartmentId(doctor.getDepartmentId());
        doctorVo.setDepartmentName(department == null ? "" : department.getName());
        doctorVo.setDoctorName(doctor.getName());
        doctorVo.setAvatar(doctor.getAvatar());
        doctorVo.setDoctorPositionId(String.valueOf(doctor.getDoctorPositionId()));
        doctorVo.setPositionName(doctorPosition == null ? "" : doctorPosition.getName());
        doctorVo.setDegreeId(String.valueOf(doctor.getDegreeId()));
        doctorVo.setDegreeName(degree == null ? "" : degree.getName());
        doctorVo.setAddress(doctor.getAddress());
        doctorVo.setScope(doctor.getScope());
        doctorVo.setTags(doctor.getTags());
        doctorVo.setDescription(doctor.getDescription());
        doctorVo.setStatus(doctor.getStatus());
        return Result.success(doctorVo);
    }

    /**
     * 获取挂号医生列表
     * @param departmentId 科室ID
     * @return  挂号医生列表
     */
    public Result<List<RegistrationDoctorBaseInfo>> getRegistrationDoctorBaseInfoByDepartmentId(Long departmentId) {
        // 获取部门挂号配置，如果为空，则获取门诊默认挂号配置
        List<AppointmentRule> appointmentRules = appointmentRuleService.listAppointmentsRulesByDepartmentId(departmentId);


        // 如果为空，则获取所有挂号配置
        if (appointmentRules.isEmpty()) {
            // 获取默认的门诊挂号配置
            appointmentRules = appointmentRuleService.listAllAppointmentRules();
        }

        // 获取可用的挂号配置
        List<AppointmentRule> enableAppointmentRules = appointmentRules
                .stream()
                .filter(item -> Objects.equals(item.getStatus(), AppointmentRuleStatusEnum.NORMAL.getCode()))    // 获取正常状态的挂号配置
                .filter(item -> Objects.equals(item.getRuleType(), AppointmentRuleTypeEnum.OUT_PATIENT.getCode())) // 获取门诊挂号配置
                .filter(item -> Objects.equals(item.getDepartmentId(), departmentId) || Objects.equals(item.getDepartmentId(), null)) // 获取指定科室的挂号配置,如果没有则获取所有科室的挂号配置
                .sorted(Comparator.comparing(AppointmentRule::getPriority, Comparator.nullsLast(Integer::compare)).reversed())
                .toList();

        if (enableAppointmentRules.isEmpty()) {
            return Result.info(404,"没有可用的挂号配置",null);
        }

        // 获取优先级最高的挂号配置
        AppointmentRule currentAppointmentRules = enableAppointmentRules.get(0);

        // 获取部门下所有医生id
        List<Long> doctorIdListInCurrentDepartment = doctorService.listDoctorsByDepartmentId(departmentId)
                .stream()
                .map(Doctor::getId)
                .toList();

        // 获取可挂号的医生列表
        List<Doctor> doctorList = doctorService.listDoctorsByDoctorIdsAndStatusAndMaxAdvanceDays(doctorIdListInCurrentDepartment, AppointmentRuleStatusEnum.NORMAL.getCode(), currentAppointmentRules.getMaxAdvanceDays());

        log.debug("doctorList by departmentId={}", doctorList);
        Department department = departmentService.getDepartmentById(departmentId);

        List<RegistrationScheduleTemplate> recentRegistrationListByDoctorIdList = registrationScheduleService.getRecentRegistrationListByDoctorIdList(
                doctorList
                        .stream()
                        .map(Doctor::getId)
                        .toList());

        log.debug("doctorList size={}, scheduleList size={}", doctorList.size(), recentRegistrationListByDoctorIdList.size());
        List<DoctorPosition> doctorPositions = doctorPositionService.listDoctorPositions();
        List<RegistrationDoctorBaseInfo> result = new ArrayList<>();
        for (Doctor doctor : doctorList) {
            RegistrationDoctorBaseInfo temp = new RegistrationDoctorBaseInfo();
            temp.setDoctorId(String.valueOf(doctor.getId()));
            temp.setDoctorName(doctor.getName());
            temp.setDepartmentId(String.valueOf(doctor.getDepartmentId()));
            temp.setDepartmentName(department == null ? "" : department.getName());
            String tags = doctor.getTags();
            temp.setTags(tags == null || tags.isEmpty()
                    ? List.of()
                    : Arrays.stream(tags.split(",")).filter(s -> !s.isEmpty()).toList());
            temp.setDescription(doctor.getDescription());
            temp.setAvatar(doctor.getAvatar());
            DoctorPosition position = doctor.getDoctorPositionId() == null ? null : doctorPositions.stream()
                    .filter(p -> p.getId().equals(doctor.getDoctorPositionId())).findFirst().orElse(null);
            temp.setPosition(position == null ? "" : position.getName());
           for (RegistrationScheduleTemplate template : recentRegistrationListByDoctorIdList){
               if (template.getDoctorId().equals(doctor.getId())){
                   // 数据库存储为"分"，直接返回
                   int price = template.getPrice() == null ? 0 : template.getPrice();
                   temp.setPrice(BigDecimal.valueOf(price));
                break;
               }
           }
           temp.setScore(doctor.getScope());
           result.add(temp);
        }
        return Result.success(result);
    }

    public Result<RegistrationDoctorConfirmVo> getRegistrationDoctorConfirmInfo(Long doctorId) {
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor == null) {
            return Result.fail("医生不存在");
        }
        Department department = doctor.getDepartmentId() == null ? null : departmentService.getDepartmentById(doctor.getDepartmentId());
        DoctorPosition doctorPosition = doctor.getDoctorPositionId() == null ? null : doctorPositionService.getPositionById(doctor.getDoctorPositionId());
        return Result.success(new RegistrationDoctorConfirmVo(
                String.valueOf(doctor.getId()),
                doctor.getName(),
                String.valueOf(doctor.getDepartmentId()),
                department == null ? "" : department.getName(),
                doctor.getAvatar(),
                String.valueOf(doctor.getDoctorPositionId()),
                doctorPosition == null ? "" : doctorPosition.getName()
        ));
    }

    public Result<List<AdminDoctorSimpleResponse>> listDoctorsSimpleResponseByDoctorName(String name){
        List<Doctor> doctorList = doctorService.listDoctorsSimpleResponseByDoctorName(name);
        List<AdminDoctorSimpleResponse> result = doctorList
                .stream()
                .map(item -> {
                    Department dept = item.getDepartmentId() == null ? null : departmentService.getDepartmentById(item.getDepartmentId());
                    return new AdminDoctorSimpleResponse(
                        String.valueOf(item.getId()),
                        item.getName(),
                        String.valueOf(item.getDepartmentId()),
                        dept == null ? "" : dept.getName()
                    );
                })
                .toList();
        return Result.success(result);
    }

    /**
     * 根据医生名称和科室id分页查询医生信息
     * @param username 医生名称
     * @param departmentId 科室id
     * @param current 当前页
     * @param size 每页数量
     * @return 医生信息
     */
    public Result<PageResult<AdminDoctorDetailResponse>> listDoctorsSimpleResponseByLikeDoctorNameAndDepartmentIdAndPage(String username, Long departmentId, Integer current, Integer size) {
        int pageNum = current == null || current < 1 ? 1 : current;
        int pageSize = size == null || size < 1 ? 10 : size;
        PageHelper.startPage(pageNum, pageSize);
        List<Doctor> doctors = doctorService.listDoctorsSimpleResponseByLikeDoctorNameAndDepartmentId(username, departmentId);
        PageInfo<Doctor> doctorsPageInfo = new PageInfo<>( doctors);
        List<Department> departments = departmentService.listAllNonParentDepartments();
        List<DoctorPosition> positions = doctorPositionService.listDoctorPositions();
        List<Degree> degrees = degreeService.listAllDegrees();

        return Result.success(new PageResult<>(doctorsPageInfo, mergeAdminDoctorDetailResponseByDoctorsAndDepartmentAndPositionAndDegrees(doctors, departments, positions, degrees)));

    }


    /**
     * 合并医生详细响应信息通过doctor,department,position,degree
     * @param doctors 医生列表
     * @param departments 科室列表
     * @param positions 职位列表
     * @param degrees 学历列表
     * @return 合并后的结果
     */
    public List<AdminDoctorDetailResponse> mergeAdminDoctorDetailResponseByDoctorsAndDepartmentAndPositionAndDegrees(List<Doctor> doctors, List<Department> departments, List<DoctorPosition> positions, List<Degree> degrees) {
        List<AdminDoctorDetailResponse> result = new ArrayList<>();
        for (Doctor doctor : doctors) {
            AdminDoctorDetailResponse item = new AdminDoctorDetailResponse();
            Department department = findDepartment(departments, doctor.getDepartmentId());
            DoctorPosition position = findPosition(positions, doctor.getDoctorPositionId());
            Degree degree = findDegree(degrees, doctor.getDegreeId());
            item.setDoctorId(String.valueOf(doctor.getId()));
            item.setDoctorName(doctor.getName());
            item.setDepartmentId(String.valueOf(doctor.getDepartmentId()));
            item.setDepartmentName(department == null ? null : department.getName());
            item.setPositionId(String.valueOf(doctor.getDoctorPositionId()));
            item.setPositionName(position == null ? null : position.getName());
            item.setDegreeId(String.valueOf(doctor.getDegreeId()));
            item.setDegreeName(degree == null ? null : degree.getName());
            item.setAvatar(doctor.getAvatar());
            item.setAddress(doctor.getAddress());
            item.setScope(doctor.getScope() == null ? null : String.valueOf(doctor.getScope()));
            String tags = doctor.getTags();
            item.setTags(tags == null || tags.isEmpty() ? List.of() : Arrays.stream(tags.split(",")).toList());
            item.setDescription(doctor.getDescription());
            item.setStatus(doctor.getStatus() == null ? null : String.valueOf(doctor.getStatus()));
            result.add(item);
        }

        return result;

    }

    private Department findDepartment(List<Department> list, Long id) {
        if (id == null || list == null) return null;
        return list.stream().filter(d -> id.equals(d.getId())).findFirst().orElse(null);
    }

    private DoctorPosition findPosition(List<DoctorPosition> list, Long id) {
        if (id == null || list == null) return null;
        return list.stream().filter(d -> id.equals(d.getId())).findFirst().orElse(null);
    }

    private Degree findDegree(List<Degree> list, Long id) {
        if (id == null || list == null) return null;
        return list.stream().filter(d -> id.equals(d.getId())).findFirst().orElse(null);
    }

    /**
     * 医生叫号接诊（status 5 → 6）
     * @param regId 挂号记录ID
     * @param doctorId 当前医生ID
     * @throws BizException REGISTRATION_NOT_FOUND / DOCTOR_NOT_MATCH / REGISTRATION_STATUS_INVALID
     */
    @Transactional(rollbackFor = Exception.class)
    public void callPatient(Long regId, Long doctorId) {
        Registration reg = registrationService.getRegistrationById(regId);
        if (reg == null) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_FOUND);
        }
        // B09: 与 MedicalRecordManager.assertDoctorOwnsRegistration 对齐，
        // 统一以 registration_schedule_template.doctor_id 为唯一来源，
        // 避免排班生成时 schedule.doctor_id 未正确回填导致叫号与开方/病历校验不一致
        if (reg.getRegistrationScheduleId() == null) {
            throw new BizException(BizErrorCode.DOCTOR_NOT_MATCH, "挂号记录无排班信息");
        }
        RegistrationSchedule schedule = registrationScheduleService.getRegistrationScheduleById(reg.getRegistrationScheduleId());
        if (schedule == null || schedule.getRegistrationScheduleTemplateId() == null) {
            throw new BizException(BizErrorCode.DOCTOR_NOT_MATCH, "排班或模板不存在");
        }
        RegistrationScheduleTemplate template = registrationScheduleTemplateService
                .getRegistrationScheduleTemplateById(schedule.getRegistrationScheduleTemplateId());
        if (template == null || !doctorId.equals(template.getDoctorId())) {
            throw new BizException(BizErrorCode.DOCTOR_NOT_MATCH);
        }
        if (!Integer.valueOf(RegistrationStatusEnum.REPORTED.getCode()).equals(reg.getStatus())) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID, "该挂号未报到");
        }
        statusLogManager.transition(reg, RegistrationStatusEnum.IN_TREATMENT.getCode(),
                doctorId, "doctor", "叫号接诊");
    }

    /**
     * 医生今日排班列表（按 registration_schedule_template.doctorId 过滤）
     * @param doctorId 医生ID
     * @return 当日挂号 VO 列表
     */
    public List<DoctorScheduleVO> listTodaySchedule(Long doctorId) {
        List<RegistrationSchedule> schedules = registrationScheduleService
                .getRegistrationSchedulesByDoctorIdAndDate(doctorId, LocalDate.now(ZoneId.of("Asia/Shanghai")));
        if (schedules == null || schedules.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> scheduleIds = schedules.stream()
                .map(RegistrationSchedule::getId).collect(Collectors.toList());
        Map<Long, RegistrationSchedule> scheduleMap = schedules.stream()
                .collect(Collectors.toMap(RegistrationSchedule::getId, s -> s));
        List<Long> templateIds = schedules.stream()
                .map(RegistrationSchedule::getRegistrationScheduleTemplateId).distinct().collect(Collectors.toList());
        Map<Long, RegistrationScheduleTemplate> templateMap = registrationScheduleTemplateService
                .listAllRegistrationScheduleTemplateByIdList(templateIds).stream()
                .collect(Collectors.toMap(RegistrationScheduleTemplate::getId, t -> t, (t1, t2) -> t1));
        List<Registration> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<Registration>()
                        .in(Registration::getRegistrationScheduleId, scheduleIds)
                        .and(w -> w.eq(Registration::getStatus, RegistrationStatusEnum.SUCCESS.getCode())
                                .or().eq(Registration::getStatus, RegistrationStatusEnum.REPORTED.getCode()))
                        .orderByAsc(Registration::getRegistrationTime));
        Map<Long, User> userMap = batchLoadUsers(registrations);
        Map<Long, Account> accountMap = batchLoadAccounts(registrations);
        return registrations.stream().map(reg -> {
            DoctorScheduleVO vo = new DoctorScheduleVO();
            vo.setRegistrationId(String.valueOf(reg.getId()));
            vo.setStatus(reg.getStatus());
            vo.setRegistrationTime(reg.getRegistrationTime());
            RegistrationSchedule s = scheduleMap.get(reg.getRegistrationScheduleId());
            if (s != null) {
                RegistrationScheduleTemplate t = templateMap.get(s.getRegistrationScheduleTemplateId());
                if (t != null) {
                    vo.setShiftName(t.getName());
                }
                vo.setStartTime(s.getStartTime());
                vo.setEndTime(s.getEndTime());
            }
            fillPatientInfo(vo, reg, userMap, accountMap);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 医生待叫号列表（status=REPORTED）
     * @param doctorId 医生ID
     * @return 已报到待叫号 VO 列表
     */
    public List<WaitingPatientVO> listWaiting(Long doctorId) {
        List<Registration> registrations = listRegistrationsByDoctorIdAndStatus(
                doctorId, RegistrationStatusEnum.REPORTED.getCode());
        Map<Long, User> userMap = batchLoadUsers(registrations);
        Map<Long, Account> accountMap = batchLoadAccounts(registrations);
        return registrations.stream().map(reg -> toWaitingVO(reg, userMap, accountMap))
                .collect(Collectors.toList());
    }

    /**
     * 医生就诊中列表（status=IN_TREATMENT，按当前医生过滤）
     * @param doctorId 医生ID
     * @return 就诊中 VO 列表
     */
    public List<WaitingPatientVO> listInProgress(Long doctorId) {
        List<Long> scheduleIds = getScheduleIdsByDoctor(doctorId);
        if (scheduleIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<Registration> registrations = registrationMapper.selectList(
                new LambdaQueryWrapper<Registration>()
                        .in(Registration::getRegistrationScheduleId, scheduleIds)
                        .eq(Registration::getStatus, RegistrationStatusEnum.IN_TREATMENT.getCode())
                        .orderByAsc(Registration::getCheckInTime));
        Map<Long, User> userMap = batchLoadUsers(registrations);
        Map<Long, Account> accountMap = batchLoadAccounts(registrations);
        return registrations.stream().map(reg -> toWaitingVO(reg, userMap, accountMap))
                .collect(Collectors.toList());
    }

    private List<Long> getScheduleIdsByDoctor(Long doctorId) {
        List<RegistrationSchedule> schedules = registrationScheduleService
                .getRegistrationSchedulesByDoctorIdAndDate(doctorId, LocalDate.now(ZoneId.of("Asia/Shanghai")));
        if (schedules == null || schedules.isEmpty()) {
            return Collections.emptyList();
        }
        return schedules.stream()
                .map(RegistrationSchedule::getId)
                .collect(Collectors.toList());
    }

    private List<Registration> listRegistrationsByDoctorIdAndStatus(Long doctorId, Integer status) {
        List<RegistrationSchedule> schedules = registrationScheduleService
                .getRegistrationSchedulesByDoctorIdAndDate(doctorId, LocalDate.now(ZoneId.of("Asia/Shanghai")));
        if (schedules == null || schedules.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> scheduleIds = schedules.stream()
                .map(RegistrationSchedule::getId).collect(Collectors.toList());
        return registrationMapper.selectList(
                new LambdaQueryWrapper<Registration>()
                        .in(Registration::getRegistrationScheduleId, scheduleIds)
                        .eq(Registration::getStatus, status)
                        .orderByAsc(Registration::getCheckInTime));
    }

    private WaitingPatientVO toWaitingVO(Registration reg, Map<Long, User> userMap, Map<Long, Account> accountMap) {
        WaitingPatientVO vo = new WaitingPatientVO();
        vo.setRegistrationId(String.valueOf(reg.getId()));
        vo.setPatientId(String.valueOf(reg.getUserId()));
        vo.setStatus(reg.getStatus());
        vo.setCheckInTime(reg.getCheckInTime());
        vo.setRegistrationTime(reg.getRegistrationTime());
        User user = userMap.get(reg.getUserId());
        Account account = accountMap.get(reg.getUserId());
        vo.setPatientName(user != null ? user.getNickname() : null);
        vo.setPatientPhone(account != null ? account.getPhone() : null);
        return vo;
    }

    private void fillPatientInfo(DoctorScheduleVO vo, Registration reg,
                                  Map<Long, User> userMap, Map<Long, Account> accountMap) {
        vo.setPatientId(String.valueOf(reg.getUserId()));
        User user = userMap.get(reg.getUserId());
        Account account = accountMap.get(reg.getUserId());
        vo.setPatientName(user != null ? user.getNickname() : null);
        vo.setPatientPhone(account != null ? account.getPhone() : null);
    }

    /** 批量加载用户信息（userId → User） */
    private Map<Long, User> batchLoadUsers(List<Registration> registrations) {
        List<Long> userIds = registrations.stream()
                .map(Registration::getUserId).distinct().collect(Collectors.toList());
        if (userIds.isEmpty()) return Collections.emptyMap();
        return userService.listUsersByUserIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
    }

    /** 批量加载账户信息（userId → Account，同一 userId 多账号时保留第一个） */
    private Map<Long, Account> batchLoadAccounts(List<Registration> registrations) {
        List<Long> userIds = registrations.stream()
                .map(Registration::getUserId).distinct().collect(Collectors.toList());
        if (userIds.isEmpty()) return Collections.emptyMap();
        return accountService.listAccountsByUserIds(userIds).stream()
                .collect(Collectors.toMap(Account::getUserId, a -> a, (a1, a2) -> a1));
    }
}
