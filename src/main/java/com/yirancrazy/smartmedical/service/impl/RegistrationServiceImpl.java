package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
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
        // B13: 强制 Asia/Shanghai 时区
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Shanghai"));
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
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
     * @param registrationScheduleId 挂号排班模板id
     * @param userId 用户id
     * @return 挂号记录
     */
    @Override
    public Registration getRegistrationByRegistrationScheduleIdAndUserId(Long registrationScheduleId, Long userId) {
        List<Registration> list = registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getRegistrationScheduleId, registrationScheduleId)
                .eq(Registration::getUserId, userId)
                .ne(Registration::getStatus, RegistrationStatusEnum.CANCELED.getCode())
                .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Registration> listRegistrationsByUserId(Long userId) {
        return registrationMapper.selectList(new QueryWrapper<Registration>().eq("user_id", userId));
    }

    @Override
    public List<Registration> listRegistrationsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .in(Registration::getUserId, userIds)
                .orderByDesc(Registration::getCreateTime));
    }

    @Override
    public List<Registration> listRegistrationsByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .in(Registration::getId, ids));
    }

    /**
     * 根据订单ID获取挂号信息
     * @param orderId 订单ID
     * @return 挂号信息
     */
    @Override
    public Registration getRegistrationByOrderId(Long orderId) {
        List<Registration> list = registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getOrderId, orderId)
                .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }
}
