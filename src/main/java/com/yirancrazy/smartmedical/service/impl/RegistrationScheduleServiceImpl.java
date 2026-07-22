package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.RegistrationScheduleMapper;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.service.RegistrationScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班服务实现类
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class RegistrationScheduleServiceImpl implements RegistrationScheduleService {

    private final RegistrationScheduleMapper registrationScheduleMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertRegistrationSchedule(RegistrationSchedule registrationSchedule) {
        return registrationScheduleMapper.insert(registrationSchedule);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RegistrationSchedule getRegistrationScheduleById(Long id) {
        return registrationScheduleMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer updateRegistrationScheduleById(RegistrationSchedule registrationSchedule) {
        return registrationScheduleMapper.updateById(registrationSchedule);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deleteRegistrationScheduleById(Long id) {
        return registrationScheduleMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RegistrationSchedule> listAllRegistrationSchedules() {
        return registrationScheduleMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<RegistrationSchedule> listRegistrationSchedulesByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<RegistrationSchedule> registrationSchedules = registrationScheduleMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(registrationSchedules);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deleteBatch(List<Long> ids) {
        return registrationScheduleMapper.deleteBatchIds(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RegistrationSchedule> getRegistrationSchedulesByDoctorId(Long doctorId) {
        return registrationScheduleMapper.selectList(new QueryWrapper<RegistrationSchedule>().eq("doctor_id", doctorId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RegistrationSchedule> getRegistrationSchedulesByStatus(Integer status) {
        return registrationScheduleMapper.selectList(new QueryWrapper<RegistrationSchedule>().eq("status", status));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getRegistrationScheduleCount() {
        return registrationScheduleMapper.selectCount(new LambdaQueryWrapper<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RegistrationSchedule> getRecentRegistrationListByDoctorIdList(List<Long> doctorIdList) {
        return registrationScheduleMapper.getRecentRegistrationListByDoctorIdList(doctorIdList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RegistrationSchedule> getRegistrationScheduleLastWeekByDoctorId(Long doctorId) {
        return registrationScheduleMapper.getRegistrationScheduleLastWeekByDoctorId(doctorId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RegistrationSchedule> getRegistrationSchedulesByDoctorIdAndDate(Long doctorId, LocalDate date) {
        return registrationScheduleMapper.getRegistrationSchedulesByDoctorIdAndDate(doctorId, date);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<RegistrationSchedule> listRegistrationSchedulesByIds(List<Long> registrationScheduleIdList) {
        return registrationScheduleMapper.selectByIds(registrationScheduleIdList);
    }

    @Override
    public List<RegistrationSchedule> listRegistrationScheduleByRegistrationScheduleIdList(List<Long> registrationScheduleIdList) {
        return registrationScheduleMapper.selectList(new LambdaQueryWrapper<RegistrationSchedule>().in(RegistrationSchedule::getRegistrationScheduleTemplateId, registrationScheduleIdList));
    }

    @Override
    public Long insertRegistrationScheduleList(List<RegistrationSchedule> list) {
        registrationScheduleMapper.insert(list);
        return (long) list.size();
    }

    @Override
    public List<RegistrationSchedule> getRegistrationScheduleListByRegistrationScheduleId(Long id) {
        return registrationScheduleMapper.selectList(new LambdaQueryWrapper<RegistrationSchedule>()
                .eq(RegistrationSchedule::getRegistrationScheduleTemplateId, id));
    }

    @Override
    public List<RegistrationSchedule> getRegistrationScheduleListByRegistrationScheduleIdList(List<Long> idList) {
        if (idList == null || idList.isEmpty()) {
            return new ArrayList<>();
        }
        return registrationScheduleMapper.selectList(new LambdaQueryWrapper<RegistrationSchedule>()
                .in(RegistrationSchedule::getRegistrationScheduleTemplateId, idList));
    }
}
