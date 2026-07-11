package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.RegistrationMapper;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号记录服务实现类
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationMapper registrationMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertRegistration(Registration registration) {
        return registrationMapper.insert(registration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Registration getRegistrationById(Long id) {
        return registrationMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer updateRegistrationById(Registration registration) {
        return registrationMapper.updateById(registration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deleteRegistrationById(Long id) {
        return registrationMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Registration> listAllRegistrations() {
        return registrationMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<Registration> listRegistrationsByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Registration> registrations = registrationMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(registrations);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deleteBatch(List<Long> ids) {
        return registrationMapper.deleteBatchIds(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getTodayRegistrationCount() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().plusDays(1).atStartOfDay();
        QueryWrapper<Registration> queryWrapper = new QueryWrapper<>();
        queryWrapper.between("registration_time", todayStart, todayEnd);
        return registrationMapper.selectCount(queryWrapper).intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getRegistrationCountByTime(String startTime, String endTime) {
        return 0;
    }

    @Override
    public Integer getRegistrationCountByDoctorAndTime(String doctorId, String startTime, String endTime) {
        return 0;
    }

    @Override
    public Integer getRegistrationCountByDepartmentAndTime(Long departmentId, String startTime, String endTime) {
        return 0;
    }

    /**
     * 根据挂号排班模板id和用户id获取挂号记录
     * @param registrationScheduleTemplateId 挂号排班模板id
     * @param userId 用户id
     * @return 挂号记录
     */
    @Override
    public Registration getRegistrationByRegistrationScheduleTemplateIdAndUserId(Long registrationScheduleTemplateId, Long userId) {
        return registrationMapper.selectOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getRegistrationScheduleTemplateId, registrationScheduleTemplateId)
                .eq(Registration::getUserId, userId));
    }

    @Override
    public List<Registration> listRegistrationsByUserId(Long userId) {
        return registrationMapper.selectList(new QueryWrapper<Registration>().eq("user_id", userId));
    }
}
