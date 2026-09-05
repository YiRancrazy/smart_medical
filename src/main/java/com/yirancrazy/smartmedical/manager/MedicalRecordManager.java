package com.yirancrazy.smartmedical.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Department;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.Prescription;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.User;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.dto.admin.request.MedicalRecordQueryRequest;
import com.yirancrazy.smartmedical.pojo.dto.admin.response.MedicalRecordPageItemVO;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.DraftMedicalRecordRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.MedicalRecordDetailVO;
import com.yirancrazy.smartmedical.pojo.dto.user.response.MedicalRecordListVO;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.DepartmentService;
import com.yirancrazy.smartmedical.service.DoctorService;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.PrescriptionService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 病历业务编排
 * @Author: YiRanCrazy@gmail.com
 * @Description: 草稿 CRUD + 转换 VO
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class MedicalRecordManager {

    private final MedicalRecordService medicalRecordService;
    private final RegistrationService registrationService;
    private final RegistrationScheduleService registrationScheduleService;
    private final RegistrationScheduleTemplateService registrationScheduleTemplateService;
    private final UserService userService;
    private final AccountService accountService;
    private final DoctorService doctorService;
    private final DepartmentService departmentService;
    private final PrescriptionService prescriptionService;
    private final PatientManager patientManager;

    /**
     * 保存病历草稿（status=0）
     * @param req 病历草稿请求
     * @param doctorId 当前医生ID（来自 JWT context）
     * @throws BizException REGISTRATION_NOT_FOUND / REGISTRATION_STATUS_INVALID / DOCTOR_NOT_MATCH / MEDICAL_RECORD_ALREADY_SUBMITTED
     */
    @Transactional(rollbackFor = Exception.class)
    public void draft(DraftMedicalRecordRequest req, Long doctorId) {
        Registration reg = registrationService.getRegistrationById(req.getRegistrationId());
        if (reg == null) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_FOUND);
        }
        assertDoctorOwnsRegistration(reg, doctorId);
        // 仅就诊中可编辑病历草稿
        if (reg.getStatus() == null
                || reg.getStatus() != RegistrationStatusEnum.IN_TREATMENT.getCode()) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID,
                    "仅就诊中状态可编辑病历");
        }
        MedicalRecord record = medicalRecordService.getOne(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getRegistrationId, req.getRegistrationId())
                        .last("LIMIT 1"));
        if (record == null) {
            record = new MedicalRecord();
            // ponytail: 不预填 id，@TableId(ASSIGN_ID) 在 save 时自动生成雪花 id；预填会导致下方 getId()==null 判断失效，新病历误走 updateById 静默失败
            record.setRegistrationId(req.getRegistrationId());
            RegistrationSchedule schedule = registrationScheduleService.getRegistrationScheduleById(reg.getRegistrationScheduleId());
            RegistrationScheduleTemplate template = schedule == null ? null
                    : registrationScheduleTemplateService.getRegistrationScheduleTemplateById(schedule.getRegistrationScheduleTemplateId());
            record.setDoctorId(template == null ? null : template.getDoctorId());
            record.setPatientId(reg.getUserId());
            record.setStatus(0);
        }
        if (record.getStatus() != null && record.getStatus() == 1) {
            throw new BizException(BizErrorCode.MEDICAL_RECORD_ALREADY_SUBMITTED);
        }
        record.setChiefComplaint(req.getChiefComplaint());
        record.setPresentIllness(req.getPresentIllness());
        record.setPastHistory(req.getPastHistory());
        record.setPhysicalExam(req.getPhysicalExam());
        record.setDiagnosis(req.getDiagnosis());
        record.setTreatmentPlan(req.getTreatmentPlan());
        if (record.getId() == null) {
            medicalRecordService.save(record);
        } else {
            medicalRecordService.updateById(record);
        }
    }

    /**
     * 病历实体转详情 VO
     * @param record 病历实体
     * @return 详情 VO
     */
    public MedicalRecordDetailVO toDetailVO(MedicalRecord record) {
        if (record == null) {
            return null;
        }
        MedicalRecordDetailVO vo = new MedicalRecordDetailVO();
        vo.setId(record.getId());
        vo.setRegistrationId(record.getRegistrationId());
        vo.setDoctorId(record.getDoctorId());
        vo.setPatientId(record.getPatientId());
        fillPatientInfo(vo, record.getPatientId());
        vo.setChiefComplaint(record.getChiefComplaint());
        vo.setPresentIllness(record.getPresentIllness());
        vo.setPastHistory(record.getPastHistory());
        vo.setPhysicalExam(record.getPhysicalExam());
        vo.setDiagnosis(record.getDiagnosis());
        vo.setTreatmentPlan(record.getTreatmentPlan());
        vo.setStatus(record.getStatus());
        return vo;
    }

    private void fillPatientInfo(MedicalRecordDetailVO vo, Long patientId) {
        if (patientId == null) {
            return;
        }
        User user = userService.getUserById(patientId);
        if (user != null) {
            vo.setPatientName(user.getNickname());
        }
        Account account = accountService.getAccountByUserId(patientId);
        if (account != null) {
            vo.setPatientPhone(account.getPhone());
        }
    }

    /**
     * 批量将病历实体转换为用户端列表 VO，补充科室/医生/就诊日期/处方ID
     * 批量查询消除 N+1
     * @param records 病历列表
     * @return 列表 VO 列表
     */
    public List<MedicalRecordListVO> toListVOs(List<MedicalRecord> records) {
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> patientIds = records.stream().map(MedicalRecord::getPatientId).distinct().collect(Collectors.toList());
        List<Long> doctorIds = records.stream().map(MedicalRecord::getDoctorId).distinct().collect(Collectors.toList());
        List<Long> registrationIds = records.stream().map(MedicalRecord::getRegistrationId).distinct().collect(Collectors.toList());
        List<Long> recordIds = records.stream().map(MedicalRecord::getId).distinct().collect(Collectors.toList());

        Map<Long, User> userMap = userService.listUsersByUserIds(patientIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Doctor> doctorMap = doctorService.listDoctorsByIds(doctorIds).stream()
                .collect(Collectors.toMap(Doctor::getId, d -> d));
        List<Long> departmentIds = doctorMap.values().stream().map(Doctor::getDepartmentId).distinct().collect(Collectors.toList());
        Map<Long, Department> departmentMap = departmentService.listDepartmentsByIds(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, d -> d));
        Map<Long, Registration> registrationMap = registrationService.listRegistrationsByIds(registrationIds).stream()
                .collect(Collectors.toMap(Registration::getId, r -> r));
        List<Long> scheduleIds = registrationMap.values().stream()
                .map(Registration::getRegistrationScheduleId).distinct().collect(Collectors.toList());
        Map<Long, RegistrationSchedule> scheduleMap = registrationScheduleService.listRegistrationSchedulesByIds(scheduleIds).stream()
                .collect(Collectors.toMap(RegistrationSchedule::getId, s -> s));
        Map<Long, Prescription> prescriptionMap = prescriptionService.list(
                new LambdaQueryWrapper<Prescription>().in(Prescription::getMedicalRecordId, recordIds))
                .stream().collect(Collectors.toMap(Prescription::getMedicalRecordId, p -> p, (p1, p2) -> p1));

        return records.stream().map(record -> {
            MedicalRecordListVO vo = new MedicalRecordListVO();
            vo.setId(record.getId());
            vo.setRegistrationId(record.getRegistrationId());
            vo.setDiagnosis(record.getDiagnosis());
            vo.setCreatedAt(record.getCreateTime());

            User user = userMap.get(record.getPatientId());
            if (user != null) {
                vo.setPatientName(user.getNickname());
            }

            Doctor doctor = doctorMap.get(record.getDoctorId());
            if (doctor != null) {
                vo.setDoctorName(doctor.getName());
                Department dept = departmentMap.get(doctor.getDepartmentId());
                if (dept != null) {
                    vo.setDepartmentName(dept.getName());
                }
            }

            Registration reg = registrationMap.get(record.getRegistrationId());
            if (reg != null) {
                RegistrationSchedule schedule = scheduleMap.get(reg.getRegistrationScheduleId());
                if (schedule != null) {
                    vo.setVisitDate(schedule.getStartTime());
                }
            }

            Prescription prescription = prescriptionMap.get(record.getId());
            if (prescription != null) {
                vo.setPrescriptionId(prescription.getId());
            }

            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 医生端 - 按挂号ID取病历（仅查询）
     * ponytail: 单表查询，直接返回实体
     * @param registrationId 挂号记录ID
     * @param doctorId 当前医生ID（来自 JWT context）
     * @return 病历详情 VO
     * @throws BizException REGISTRATION_NOT_FOUND / DOCTOR_NOT_MATCH
     */
    public MedicalRecordDetailVO getByRegistrationId(Long registrationId, Long doctorId) {
        Registration reg = registrationService.getRegistrationById(registrationId);
        if (reg == null) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_FOUND);
        }
        assertDoctorOwnsRegistration(reg, doctorId);
        MedicalRecord record = medicalRecordService.getOne(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getRegistrationId, registrationId)
                        .last("LIMIT 1"));
        return toDetailVO(record);
    }

    /**
     * 校验当前医生是否为挂号记录对应排班的出诊医生
     * ponytail: 通过 reg → schedule → template.doctorId 链路校验，复用现有 Service
     * @param reg 挂号记录
     * @param doctorId 当前医生ID
     * @throws BizException DOCTOR_NOT_MATCH
     */
    private void assertDoctorOwnsRegistration(Registration reg, Long doctorId) {
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
    }

    /**
     * 用户端 - 病历列表（按就诊人过滤）
     * ponytail: 单表查询，直接返回实体列表
     * @param patientUserIds 可访问的用户ID列表
     * @return 病历实体列表（按创建时间倒序）
     */
    public List<MedicalRecord> listByPatientIds(List<Long> patientUserIds) {
        if (patientUserIds == null || patientUserIds.isEmpty()) {
            return Collections.emptyList();
        }
        return medicalRecordService.list(
                new LambdaQueryWrapper<MedicalRecord>()
                        .in(MedicalRecord::getPatientId, patientUserIds)
                        .orderByDesc(MedicalRecord::getCreateTime));
    }

    /**
     * 用户端 - 病历详情（含权限校验）
     * @param id 病历ID
     * @param userId 当前用户ID
     * @return 用户端病历详情 VO
     * @throws BizException MEDICAL_RECORD_NOT_FOUND / MEDICAL_RECORD_NOT_OWNED
     */
    public com.yirancrazy.smartmedical.pojo.dto.user.response.MedicalRecordDetailVO getMedicalRecordById(Long id, Long userId) {
        MedicalRecord record = medicalRecordService.getById(id);
        if (record == null) {
            throw new BizException(BizErrorCode.MEDICAL_RECORD_NOT_FOUND, "无权查看该病历");
        }
        // 复用列表端点的可访问患者集合，确保家属代查场景一致
        List<Long> accessibleUserIds = patientManager.getAccessiblePatientUserIds(userId, null);
        if (!accessibleUserIds.contains(record.getPatientId())) {
            throw new BizException(BizErrorCode.MEDICAL_RECORD_NOT_FOUND, "无权查看该病历");
        }
        return toUserDetailVO(record);
    }

    /**
     * 病历实体转用户端详情 VO
     * @param record 病历实体
     * @return 用户端详情 VO
     */
    private com.yirancrazy.smartmedical.pojo.dto.user.response.MedicalRecordDetailVO toUserDetailVO(MedicalRecord record) {
        if (record == null) {
            return null;
        }
        com.yirancrazy.smartmedical.pojo.dto.user.response.MedicalRecordDetailVO vo =
                new com.yirancrazy.smartmedical.pojo.dto.user.response.MedicalRecordDetailVO();
        vo.setId(record.getId());
        vo.setRegistrationId(record.getRegistrationId());
        fillPatientInfo(vo, record.getPatientId());
        vo.setChiefComplaint(record.getChiefComplaint());
        vo.setPresentIllness(record.getPresentIllness());
        vo.setPastHistory(record.getPastHistory());
        vo.setPhysicalExam(record.getPhysicalExam());
        vo.setDiagnosis(record.getDiagnosis());
        vo.setTreatmentPlan(record.getTreatmentPlan());
        vo.setStatus(record.getStatus());
        fillDoctorAndDepartment(vo, record.getDoctorId());
        fillVisitDateAndPrescription(vo, record);
        vo.setCreateTime(record.getCreateTime());
        return vo;
    }

    private void fillPatientInfo(com.yirancrazy.smartmedical.pojo.dto.user.response.MedicalRecordDetailVO vo, Long patientId) {
        if (patientId == null) {
            return;
        }
        User user = userService.getUserById(patientId);
        if (user != null) {
            vo.setPatientName(user.getNickname());
        }
        Account account = accountService.getAccountByUserId(patientId);
        if (account != null) {
            vo.setPatientPhone(account.getPhone());
        }
    }

    private void fillDoctorAndDepartment(com.yirancrazy.smartmedical.pojo.dto.user.response.MedicalRecordDetailVO vo, Long doctorId) {
        if (doctorId == null) {
            return;
        }
        Doctor doctor = doctorService.getDoctorById(doctorId);
        if (doctor != null) {
            vo.setDoctorName(doctor.getName());
            if (doctor.getDepartmentId() != null) {
                Department dept = departmentService.getDepartmentById(doctor.getDepartmentId());
                if (dept != null) {
                    vo.setDepartmentName(dept.getName());
                }
            }
        }
    }

    private void fillVisitDateAndPrescription(com.yirancrazy.smartmedical.pojo.dto.user.response.MedicalRecordDetailVO vo, MedicalRecord record) {
        if (record.getRegistrationId() != null) {
            Registration reg = registrationService.getRegistrationById(record.getRegistrationId());
            if (reg != null && reg.getRegistrationScheduleId() != null) {
                RegistrationSchedule schedule = registrationScheduleService
                        .getRegistrationScheduleById(reg.getRegistrationScheduleId());
                if (schedule != null) {
                    vo.setVisitDate(schedule.getStartTime());
                }
            }
        }
        Prescription prescription = prescriptionService.getOne(
                new LambdaQueryWrapper<Prescription>()
                        .eq(Prescription::getMedicalRecordId, record.getId())
                        .last("LIMIT 1"));
        if (prescription != null) {
            vo.setPrescriptionId(prescription.getId());
        }
    }

    /**
     * 管理端/医生端 - 病历历史分页查询
     * @param request 查询条件
     * @param doctorId 医生ID；null 表示查询全部（管理员/药师），非null则按医生过滤
     * @return 病历分页列表
     */
    public PageInfo<MedicalRecordPageItemVO> pageMedicalRecords(MedicalRecordQueryRequest request, Long doctorId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getDeleted, false)
                .orderByDesc(MedicalRecord::getCreateTime);

        if (doctorId != null) {
            wrapper.eq(MedicalRecord::getDoctorId, doctorId);
        }

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        if (startDate != null) {
            wrapper.ge(MedicalRecord::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(MedicalRecord::getCreateTime, endDate.atTime(LocalTime.MAX));
        }

        List<Long> patientUserIds = null;
        if (request.getPatientName() != null && !request.getPatientName().trim().isEmpty()) {
            patientUserIds = userService.listUserIdsByNicknameLike(request.getPatientName().trim());
            if (patientUserIds.isEmpty()) {
                return new PageInfo<>(Collections.emptyList());
            }
            wrapper.in(MedicalRecord::getPatientId, patientUserIds);
        }

        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        PageHelper.startPage(pageNum, pageSize);
        List<MedicalRecord> records = medicalRecordService.list(wrapper);
        return new PageInfo<>(toAdminPageItemVOs(records));
    }

    /**
     * 批量转换病历实体为管理端列表 VO，使用批量查询避免 N+1
     * @param records 病历列表
     * @return 管理端列表 VO
     */
    private List<MedicalRecordPageItemVO> toAdminPageItemVOs(List<MedicalRecord> records) {
        if (records.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> patientIds = records.stream().map(MedicalRecord::getPatientId).distinct().collect(Collectors.toList());
        List<Long> doctorIds = records.stream().map(MedicalRecord::getDoctorId).distinct().collect(Collectors.toList());
        List<Long> registrationIds = records.stream().map(MedicalRecord::getRegistrationId).distinct().collect(Collectors.toList());

        Map<Long, User> userMap = userService.listUsersByUserIds(patientIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Doctor> doctorMap = doctorService.listDoctorsByIds(doctorIds).stream()
                .collect(Collectors.toMap(Doctor::getId, d -> d));
        List<Long> departmentIds = doctorMap.values().stream().map(Doctor::getDepartmentId).distinct().collect(Collectors.toList());
        Map<Long, Department> departmentMap = departmentService.listDepartmentsByIds(departmentIds).stream()
                .collect(Collectors.toMap(Department::getId, d -> d));

        Map<Long, Registration> registrationMap = registrationService.listRegistrationsByIds(registrationIds).stream()
                .collect(Collectors.toMap(Registration::getId, r -> r));
        List<Long> scheduleIds = registrationMap.values().stream()
                .map(Registration::getRegistrationScheduleId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, RegistrationSchedule> scheduleMap = registrationScheduleService.listRegistrationSchedulesByIds(scheduleIds).stream()
                .collect(Collectors.toMap(RegistrationSchedule::getId, s -> s));

        return records.stream().map(record -> {
            MedicalRecordPageItemVO vo = new MedicalRecordPageItemVO();
            vo.setId(record.getId());
            vo.setRegistrationId(record.getRegistrationId());
            vo.setDiagnosis(record.getDiagnosis());
            vo.setCreateTime(record.getCreateTime());

            User user = userMap.get(record.getPatientId());
            if (user != null) {
                vo.setPatientName(user.getNickname());
            }

            Doctor doctor = doctorMap.get(record.getDoctorId());
            if (doctor != null) {
                vo.setDoctorName(doctor.getName());
                Department dept = departmentMap.get(doctor.getDepartmentId());
                if (dept != null) {
                    vo.setDepartmentName(dept.getName());
                }
            }

            Registration reg = registrationMap.get(record.getRegistrationId());
            if (reg != null) {
                RegistrationSchedule schedule = scheduleMap.get(reg.getRegistrationScheduleId());
                if (schedule != null) {
                    vo.setVisitDate(schedule.getStartTime());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 管理端 - 病历详情
     * @param id 病历ID
     * @return 病历详情 VO
     * @throws BizException MEDICAL_RECORD_NOT_FOUND
     */
    public com.yirancrazy.smartmedical.pojo.dto.admin.response.MedicalRecordDetailVO getMedicalRecordDetailForAdmin(Long id) {
        return getMedicalRecordDetailForAdmin(id, null);
    }

    /**
     * 病历详情（admin 传 doctorId=null 无约束；doctor 传 doctorId 校验归属，防止读取他人病历）
     * @param id 病历ID
     * @param doctorId 医生ID；非空时校验病历必须属于该医生
     * @return 病历详情 VO
     * @throws BizException MEDICAL_RECORD_NOT_FOUND / DOCTOR_NOT_MATCH
     */
    public com.yirancrazy.smartmedical.pojo.dto.admin.response.MedicalRecordDetailVO getMedicalRecordDetailForAdmin(Long id, Long doctorId) {
        MedicalRecord record = medicalRecordService.getById(id);
        if (record == null) {
            throw new BizException(BizErrorCode.MEDICAL_RECORD_NOT_FOUND);
        }
        if (doctorId != null && !doctorId.equals(record.getDoctorId())) {
            throw new BizException(BizErrorCode.DOCTOR_NOT_MATCH, "无权查看非本人病历");
        }
        com.yirancrazy.smartmedical.pojo.dto.admin.response.MedicalRecordDetailVO vo =
                new com.yirancrazy.smartmedical.pojo.dto.admin.response.MedicalRecordDetailVO();
        vo.setId(record.getId());
        vo.setRegistrationId(record.getRegistrationId());
        vo.setDoctorId(record.getDoctorId());
        vo.setPatientId(record.getPatientId());
        vo.setChiefComplaint(record.getChiefComplaint());
        vo.setPresentIllness(record.getPresentIllness());
        vo.setPastHistory(record.getPastHistory());
        vo.setPhysicalExam(record.getPhysicalExam());
        vo.setDiagnosis(record.getDiagnosis());
        vo.setTreatmentPlan(record.getTreatmentPlan());
        vo.setStatus(record.getStatus());

        User user = userService.getUserById(record.getPatientId());
        if (user != null) {
            vo.setPatientName(user.getNickname());
        }
        Account account = accountService.getAccountByUserId(record.getPatientId());
        if (account != null) {
            vo.setPatientPhone(account.getPhone());
        }
        Doctor doctor = doctorService.getDoctorById(record.getDoctorId());
        if (doctor != null) {
            vo.setDoctorName(doctor.getName());
            if (doctor.getDepartmentId() != null) {
                Department dept = departmentService.getDepartmentById(doctor.getDepartmentId());
                if (dept != null) {
                    vo.setDepartmentName(dept.getName());
                }
            }
        }
        return vo;
    }
}