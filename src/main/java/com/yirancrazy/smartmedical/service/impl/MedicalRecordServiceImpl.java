package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.MedicalRecordMapper;
import com.yirancrazy.smartmedical.pojo.MedicalRecord;
import com.yirancrazy.smartmedical.service.MedicalRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * 电子病历 Service 实现
 * @Author: YiRanCrazy@gmail.com
 * @Description: 病历 Service 实现（骨架）
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Service
public class MedicalRecordServiceImpl
        extends ServiceImpl<MedicalRecordMapper, MedicalRecord>
        implements MedicalRecordService {

    /**
     * {@inheritDoc}
     */
    @Override
    public MedicalRecord getByRegistrationId(Long registrationId) {
        if (registrationId == null) {
            return null;
        }
        return baseMapper.selectOne(new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getRegistrationId, registrationId)
                .last("LIMIT 1"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MedicalRecord> listByRegistrationIds(Collection<Long> registrationIds) {
        if (registrationIds == null || registrationIds.isEmpty()) {
            return List.of();
        }
        return baseMapper.selectList(new LambdaQueryWrapper<MedicalRecord>()
                .in(MedicalRecord::getRegistrationId, registrationIds));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MedicalRecord> listByPatientUserIds(Collection<Long> patientUserIds) {
        if (patientUserIds == null || patientUserIds.isEmpty()) {
            return List.of();
        }
        return baseMapper.selectList(new LambdaQueryWrapper<MedicalRecord>()
                .in(MedicalRecord::getPatientId, patientUserIds)
                .orderByDesc(MedicalRecord::getCreateTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MedicalRecord> listMedicalRecordsByDoctorIdAndPatientUserIds(
            Long doctorId, Collection<Long> patientUserIds) {
        if (patientUserIds != null && patientUserIds.isEmpty()) {
            return List.of();
        }
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getDeleted, false)
                .orderByDesc(MedicalRecord::getCreateTime);
        if (doctorId != null) {
            wrapper.eq(MedicalRecord::getDoctorId, doctorId);
        }
        if (patientUserIds != null) {
            wrapper.in(MedicalRecord::getPatientId, patientUserIds);
        }
        return baseMapper.selectList(wrapper);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<MedicalRecord> listMedicalRecordsByPatientUserIdsAndDoctorIdPage(
            List<Long> patientUserIds, Long doctorId, LocalDateTime createTimeStart,
            LocalDateTime createTimeEnd, Integer pageNum, Integer pageSize) {
        if (patientUserIds != null && patientUserIds.isEmpty()) {
            PageHelper.clearPage();
            return new PageInfo<>(List.of());
        }

        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<MedicalRecord>()
                .eq(MedicalRecord::getDeleted, false)
                .orderByDesc(MedicalRecord::getCreateTime);
        if (doctorId != null) {
            wrapper.eq(MedicalRecord::getDoctorId, doctorId);
        }
        if (createTimeStart != null) {
            wrapper.ge(MedicalRecord::getCreateTime, createTimeStart);
        }
        if (createTimeEnd != null) {
            wrapper.le(MedicalRecord::getCreateTime, createTimeEnd);
        }
        if (patientUserIds != null) {
            wrapper.in(MedicalRecord::getPatientId, patientUserIds);
        }

        int normalizedPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int normalizedPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        PageHelper.startPage(normalizedPageNum, normalizedPageSize);
        return new PageInfo<>(baseMapper.selectList(wrapper));
    }
}
