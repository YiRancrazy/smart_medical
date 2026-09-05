package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.RegistrationMapper;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.RegistrationStatusLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号记录服务实现类
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final RegistrationStatusLogService registrationStatusLogService;

    /**
     * 允许的状态转移白名单（from -> 可达 to 集合）
     * 对应状态机文档定义，禁止任意状态互转
     */
    private static final Map<Integer, Set<Integer>> ALLOWED_TRANSITIONS = Map.of(
            RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode(),
            Set.of(RegistrationStatusEnum.SUCCESS.getCode(),
                    RegistrationStatusEnum.FAILED.getCode(),
                    RegistrationStatusEnum.CANCELED.getCode()),
            RegistrationStatusEnum.SUCCESS.getCode(),
            Set.of(RegistrationStatusEnum.REPORTED.getCode(),
                    RegistrationStatusEnum.CANCELED.getCode()),
            RegistrationStatusEnum.FAILED.getCode(),
            Set.of(RegistrationStatusEnum.WAITING_FOR_PAYMENT.getCode(),
                    RegistrationStatusEnum.CANCELED.getCode()),
            RegistrationStatusEnum.REPORTED.getCode(),
            Set.of(RegistrationStatusEnum.IN_TREATMENT.getCode()),
            RegistrationStatusEnum.IN_TREATMENT.getCode(),
            Set.of(RegistrationStatusEnum.PENDING_PAYMENT.getCode(),
                    RegistrationStatusEnum.COMPLETED.getCode()),
            RegistrationStatusEnum.PENDING_PAYMENT.getCode(),
            Set.of(RegistrationStatusEnum.IN_TREATMENT.getCode(),
                    RegistrationStatusEnum.COMPLETED.getCode())
    );

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
        return registrationMapper.deleteByIds(ids);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public Long countTodayRegistrations(LocalDateTime start, LocalDateTime end) {
        return registrationMapper.selectCount(new QueryWrapper<Registration>()
                .ge("registration_time", start)
                .lt("registration_time", end)
                .ne("status", RegistrationStatusEnum.CANCELED.getCode()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long countByStatuses(Collection<Integer> statuses) {
        return registrationMapper.selectCount(new QueryWrapper<Registration>()
                .in("status", statuses));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long countByStatus(Integer status) {
        return registrationMapper.selectCount(new QueryWrapper<Registration>()
                .eq("status", status));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Registration> listByScheduleIdsAndStatuses(List<Long> scheduleIds, Collection<Integer> statuses) {
        return registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .in(Registration::getRegistrationScheduleId, scheduleIds)
                .in(Registration::getStatus, statuses)
                .orderByAsc(Registration::getRegistrationTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Registration> listByScheduleIdsAndStatus(List<Long> scheduleIds, Integer status) {
        return registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .in(Registration::getRegistrationScheduleId, scheduleIds)
                .eq(Registration::getStatus, status)
                .orderByAsc(Registration::getCheckInTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatusWithLog(Registration reg, int toStatus,
                                    Long operatorId, String operatorRole, String remark) {
        Integer fromStatus = reg.getStatus();
        if (fromStatus == null) {
            fromStatus = -1;
        }

        // 业务白名单校验：禁止任意状态互转
        Set<Integer> allowedTo = ALLOWED_TRANSITIONS.get(fromStatus);
        if (allowedTo == null || !allowedTo.contains(toStatus)) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID,
                    "非法状态流转：" + fromStatus + " -> " + toStatus);
        }

        // 带乐观守门的 UPDATE：WHERE id=? AND status=fromStatus
        // 使用 UpdateWrapper(字符串列名)而非 LambdaUpdateWrapper(实体方法引用)，
        // 后者依赖 MyBatis-Plus lambda cache，单元测试场景下未初始化会抛 MybatisPlusException。
        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<Registration> uw = new UpdateWrapper<Registration>()
                .eq("id", reg.getId())
                .eq("status", fromStatus)
                .set("status", toStatus);
        if (toStatus == RegistrationStatusEnum.REPORTED.getCode()) {
            uw.set("check_in_time", now);
        } else if (toStatus == RegistrationStatusEnum.IN_TREATMENT.getCode()) {
            uw.set("visit_start_time", now);
        } else if (toStatus == RegistrationStatusEnum.COMPLETED.getCode()) {
            uw.set("visit_end_time", now);
        }
        int rows = registrationMapper.update(null, uw);
        if (rows == 0) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID,
                    "状态已变更，请刷新");
        }

        // 同步内存中状态，便于后续可能继续使用 reg
        reg.setStatus(toStatus);

        // 写状态日志
        registrationStatusLogService.writeLog(reg.getId(), fromStatus, toStatus,
                operatorId, operatorRole, remark);
        log.info("[registration-status] regId={} {}->{} by {}({})",
                reg.getId(), fromStatus, toStatus, operatorRole, operatorId);
    }
}
