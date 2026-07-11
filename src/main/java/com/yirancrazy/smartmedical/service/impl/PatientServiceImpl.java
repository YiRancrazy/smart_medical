package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.PatientMapper;
import com.yirancrazy.smartmedical.pojo.Patient;
import com.yirancrazy.smartmedical.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 患者服务实现类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientMapper patientMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertPatient(Patient patient) {
        return patientMapper.insert(patient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Patient getPatientById(Long id) {
        return patientMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Patient getPatientByPatientCardId(Long patientCardId) {
        return patientMapper.selectOne(new QueryWrapper<Patient>().eq("patient_card_id", patientCardId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer updatePatientById(Patient patient) {
        return patientMapper.updateById(patient);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deletePatientById(Long id) {
        return patientMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Patient> listAllPatients() {
        return patientMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Patient> getPatientsByIds(List<Long> ids) {
        return patientMapper.selectBatchIds(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<Patient> listPatientsByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Patient> patients = patientMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(patients);
    }

    /**
     * 根据用户id获取患者
     * @param userId 用户id
     * @return 患者
     */
    @Override
    public Patient getPatientByUserId(Long userId) {
        return patientMapper.selectOne(new LambdaQueryWrapper<Patient>().eq(Patient::getUserId, userId));
    }
}
