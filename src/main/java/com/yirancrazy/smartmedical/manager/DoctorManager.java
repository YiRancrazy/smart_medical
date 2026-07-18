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
    public int addDoctor(Doctor doctor) {
        doctor.setId(IdUtil.getSnowflakeNextId());
        return doctorService.insertDoctor(doctor);
    }

    public Result<DoctorVo> getDoctorById(Long id) {
        Doctor doctor = doctorService.getDoctorById(id);

        if (doctor == null) {
            return Result.info(404,"医生不存在",null);
        }

        DoctorVo doctorVo = new DoctorVo(
                String.valueOf(doctor.getId()),
                doctor.getDepartmentId(),
                doctor.getName(),
                doctor.getAvatar(),
                String.valueOf(doctor.getDoctorPositionId()),
                String.valueOf(doctor.getDegreeId()),
                doctor.getAddress(),
                doctor.getScope(),
                doctor.getTags(),
                doctor.getDescription(),
                doctor.getStatus()
        );
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
                .sorted(new Comparator<AppointmentRule>() {
                    @Override
                    public int compare(AppointmentRule o1, AppointmentRule o2) {
                        return o2.getPriority() - o1.getPriority();
                    }
                })
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

        List<RegistrationSchedule> recentRegistrationListByDoctorIdList = registrationScheduleService.getRecentRegistrationListByDoctorIdList(
                doctorList
                        .stream()
                        .map(Doctor::getId)
                        .toList());

        log.debug("doctorList size={}, scheduleList size={}", doctorList.size(), recentRegistrationListByDoctorIdList.size());
        List<RegistrationDoctorBaseInfo> result = new ArrayList<>();
        for (Doctor doctor : doctorList) {
            RegistrationDoctorBaseInfo temp = new RegistrationDoctorBaseInfo();
            temp.setDoctorId(String.valueOf(doctor.getId()));
            temp.setDoctorName(doctor.getName());
            temp.setDepartmentId(String.valueOf(doctor.getDepartmentId()));
            temp.setDepartmentName(department == null ? "" : department.getName());
            String tags = doctor.getTags();
            temp.setTags(tags == null || tags.isEmpty() ? List.of() : Arrays.stream(tags.split(",")).toList());
            temp.setDescription(doctor.getDescription());
            temp.setAvatar(doctor.getAvatar());
            temp.setPosition(String.valueOf(doctor.getDoctorPositionId()));
           for (RegistrationSchedule registrationSchedule : recentRegistrationListByDoctorIdList){
               if (registrationSchedule.getDoctorId().equals(doctor.getId())){
//                   temp.setRecentWorkTime(registrationSchedule.getStartTime());
                   temp.setPrice(BigDecimal.ZERO);
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
        RegistrationScheduleTemplate template = registrationScheduleTemplateService
                .getRegistrationScheduleTemplateById(reg.getRegistrationScheduleTemplateId());
        Long regDoctorId = template == null ? null : template.getDoctorId();
        if (!doctorId.equals(regDoctorId)) {
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
     * @return 当日挂号列表
     */
    public List<Registration> listTodaySchedule(Long doctorId) {
        List<RegistrationScheduleTemplate> templates = registrationScheduleTemplateService
                .getRegistrationScheduleTemplateByDoctorIdAndDate(doctorId, LocalDate.now());
        if (templates == null || templates.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> templateIds = templates.stream()
                .map(RegistrationScheduleTemplate::getId)
                .collect(Collectors.toList());
        return registrationMapper.selectList(
                new LambdaQueryWrapper<Registration>()
                        .in(Registration::getRegistrationScheduleTemplateId, templateIds)
                        .orderByAsc(Registration::getRegistrationTime));
    }

    /**
     * 医生待叫号列表（status=REPORTED）
     * @param doctorId 医生ID
     * @return 已报到待叫号挂号列表
     */
    public List<Registration> listWaiting(Long doctorId) {
        List<RegistrationScheduleTemplate> templates = registrationScheduleTemplateService
                .listRegistrationScheduleTemplatesByDoctorId(doctorId);
        if (templates == null || templates.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> templateIds = templates.stream()
                .map(RegistrationScheduleTemplate::getId)
                .collect(Collectors.toList());
        return registrationMapper.selectList(
                new LambdaQueryWrapper<Registration>()
                        .in(Registration::getRegistrationScheduleTemplateId, templateIds)
                        .eq(Registration::getStatus, RegistrationStatusEnum.REPORTED.getCode())
                        .orderByAsc(Registration::getCheckInTime));
    }

    /**
     * 医生就诊中列表（status=IN_TREATMENT 或 PENDING_PAYMENT，按当前医生过滤）
     * @param doctorId 医生ID
     * @return 就诊中挂号列表
     */
    public List<Registration> listInProgress(Long doctorId) {
        List<RegistrationScheduleTemplate> templates = registrationScheduleTemplateService
                .listRegistrationScheduleTemplatesByDoctorId(doctorId);
        if (templates == null || templates.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> templateIds = templates.stream()
                .map(RegistrationScheduleTemplate::getId)
                .collect(Collectors.toList());
        return registrationMapper.selectList(
                new LambdaQueryWrapper<Registration>()
                        .in(Registration::getRegistrationScheduleTemplateId, templateIds)
                        .and(w -> w.eq(Registration::getStatus, RegistrationStatusEnum.IN_TREATMENT.getCode())
                                .or().eq(Registration::getStatus, RegistrationStatusEnum.PENDING_PAYMENT.getCode()))
                        .orderByAsc(Registration::getCheckInTime));
    }
}
