package com.yirancrazy.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 电子病历 Service
 * @Author: YiRanCrazy@gmail.com
 * @Description: 病历读写骨架，业务方法后续按需添加
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

public interface MedicalRecordService extends IService<MedicalRecord> {

    /**
     * 按挂号ID查询病历（不存在返回 null）
     * @param registrationId 挂号ID
     * @return 病历
     */
    MedicalRecord getByRegistrationId(Long registrationId);

    /**
     * 按挂号ID集合批量查询病历（空集合返回空列表）
     * @param registrationIds 挂号ID集合
     * @return 病历列表
     */
    List<MedicalRecord> listByRegistrationIds(Collection<Long> registrationIds);

    /**
     * 按患者用户ID集合查询病历（空集合返回空列表，按创建时间倒序）
     * @param patientUserIds 患者用户ID集合
     * @return 病历列表
     */
    List<MedicalRecord> listByPatientUserIds(Collection<Long> patientUserIds);

    /**
     * 按医生ID和患者用户ID集合查询病历（条件可为空）
     * @param doctorId 医生ID；null=不按医生过滤
     * @param patientUserIds 患者用户ID集合；null=不按患者过滤，空集合=无匹配
     * @return 病历列表（按创建时间倒序）
     */
    List<MedicalRecord> listMedicalRecordsByDoctorIdAndPatientUserIds(
            Long doctorId, Collection<Long> patientUserIds);

    /**
     * 分页查询病历（按患者用户ID集合或医生ID过滤，条件可为空）
     * @param patientUserIds 患者用户ID集合；null=不按患者过滤，空集合=无匹配
     * @param doctorId 医生ID；null=不按医生过滤
     * @param createTimeStart 创建时间起；null=不限制
     * @param createTimeEnd 创建时间止；null=不限制
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageInfo<MedicalRecord> listMedicalRecordsByPatientUserIdsAndDoctorIdPage(
            List<Long> patientUserIds, Long doctorId, LocalDateTime createTimeStart,
            LocalDateTime createTimeEnd, Integer pageNum, Integer pageSize);
}
