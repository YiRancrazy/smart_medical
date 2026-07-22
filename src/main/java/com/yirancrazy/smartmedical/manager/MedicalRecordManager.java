package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.dto.doctor.request.DraftMedicalRecordRequest;
import com.yirancrazy.smartmedical.pojo.dto.doctor.response.MedicalRecordDetailVO;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import com.yirancrazy.smartmedical.service.RegistrationScheduleTemplateService;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

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
}