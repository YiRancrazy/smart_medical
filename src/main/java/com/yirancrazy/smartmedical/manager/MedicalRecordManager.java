package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 病历业务编排
 * @Author: YiRanCrazy@gmail.com
 * @Description: 草稿 CRUD + 转换 VO
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

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

    /**
     * 保存病历草稿（status=0）
     * @param req 病历草稿请求
     * @throws BizException REGISTRATION_NOT_FOUND / MEDICAL_RECORD_ALREADY_SUBMITTED
     */
    @Transactional(rollbackFor = Exception.class)
    public void draft(DraftMedicalRecordRequest req) {
        Registration reg = registrationService.getRegistrationById(req.getRegistrationId());
        if (reg == null) {
            throw new BizException(BizErrorCode.REGISTRATION_NOT_FOUND);
        }
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
            record.setId(IdUtil.getSnowflakeNextId());
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
     * ponytail: 逐条查询 N+1，单用户病历列表 < 100，可接受；若量大可改为批量 IN
     * @param records 病历列表
     * @return 列表 VO 列表
     */
    public List<MedicalRecordListVO> toListVOs(List<MedicalRecord> records) {
        List<MedicalRecordListVO> result = new ArrayList<>(records.size());
        for (MedicalRecord record : records) {
            MedicalRecordListVO vo = new MedicalRecordListVO();
            vo.setId(record.getId());
            vo.setRegistrationId(record.getRegistrationId());
            vo.setDiagnosis(record.getDiagnosis());
            vo.setCreatedAt(record.getCreateTime());
            fillPatientName(vo, record.getPatientId());
            fillDoctorAndDepartment(vo, record.getDoctorId());
            fillVisitDateAndPrescription(vo, record);
            result.add(vo);
        }
        return result;
    }

    private void fillPatientName(MedicalRecordListVO vo, Long patientId) {
        if (patientId == null) {
            return;
        }
        User user = userService.getUserById(patientId);
        if (user != null) {
            vo.setPatientName(user.getNickname());
        }
    }

    private void fillDoctorAndDepartment(MedicalRecordListVO vo, Long doctorId) {
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

    private void fillVisitDateAndPrescription(MedicalRecordListVO vo, MedicalRecord record) {
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
     * 医生端 - 按挂号ID取病历（仅查询）
     * ponytail: 单表查询，直接返回实体
     * @param registrationId 挂号记录ID
     * @return 病历详情 VO
     */
    public MedicalRecordDetailVO getByRegistrationId(Long registrationId) {
        MedicalRecord record = medicalRecordService.getOne(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getRegistrationId, registrationId)
                        .last("LIMIT 1"));
        return toDetailVO(record);
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
     * @return 病历实体
     * @throws BizException MEDICAL_RECORD_NOT_FOUND / MEDICAL_RECORD_NOT_OWNED
     */
    public MedicalRecord getMedicalRecordById(Long id, Long userId) {
        MedicalRecord record = medicalRecordService.getById(id);
        if (record == null || !userId.equals(record.getPatientId())) {
            throw new BizException(BizErrorCode.MEDICAL_RECORD_NOT_FOUND, "无权查看该病历");
        }
        return record;
    }
}