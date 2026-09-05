package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Registration;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号记录服务接口
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

public interface RegistrationService {

    /**
     * 添加挂号记录
     * @param registration 挂号记录对象
     * @return 添加结果
     */
    Integer insertRegistration(Registration registration);

    /**
     * 根据ID查询挂号记录
     * @param id 挂号ID
     * @return 挂号记录对象
     */
    Registration getRegistrationById(Long id);

    /**
     * 根据ID更新挂号记录
     * @param registration 挂号记录对象
     * @return 更新结果
     */
    Integer updateRegistrationById(Registration registration);

    /**
     * 根据ID删除挂号记录
     * @param id 挂号ID
     * @return 删除结果
     */
    Integer deleteRegistrationById(Long id);

    /**
     * 查询所有挂号记录列表
     * @return 挂号记录列表
     */
    List<Registration> listAllRegistrations();

    /**
     * 分页查询挂号记录
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<Registration> listRegistrationsByPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除挂号记录
     * @param ids 挂号ID列表
     * @return 删除结果
     */
    Integer deleteBatch(List<Long> ids);

    /**
     * 获取今日挂号数量
     * @return 今日挂号数量
     */
    Integer getTodayRegistrationCount();

    /**
     * 根据时间范围获取挂号数量
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 挂号数量
     */
    Integer getRegistrationCountByTime(String startTime, String endTime);

    /**
     * 根据医生和时间范围获取挂号数量
     * @param doctorId 医生ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 挂号数量
     */
    Integer getRegistrationCountByDoctorAndTime(String doctorId, String startTime, String endTime);

    /**
     * 根据科室和时间范围获取挂号数量
     * @param departmentId 科室ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 挂号数量
     */
    Integer getRegistrationCountByDepartmentAndTime(Long departmentId, String startTime, String endTime);

    /**
     * 根据排班模板id和用户id获取挂号信息
     * @param registrationScheduleTemplateId 挂号排班模板id
     * @param userId 用户id
     * @return 挂号信息
     */
    Registration getRegistrationByRegistrationScheduleIdAndUserId(Long registrationScheduleId, Long userId);

    /**
     * 根据用户id获取挂号信息
     * @param userId 用户id
     * @return 挂号信息
     */
    List<Registration> listRegistrationsByUserId(Long userId);

    /**
     * 根据用户id列表获取挂号信息
     * @param userIds 用户id列表
     * @return 挂号信息列表
     */
    List<Registration> listRegistrationsByUserIds(List<Long> userIds);

    /**
     * 根据挂号记录ID列表批量查询挂号信息
     * @param ids 挂号记录ID列表
     * @return 挂号信息列表
     */
    List<Registration> listRegistrationsByIds(List<Long> ids);

    /**
     * 根据订单ID获取挂号信息
     * @param orderId 订单ID
     * @return 挂号信息
     */
    Registration getRegistrationByOrderId(Long orderId);

    /**
     * 统计指定时间范围内（排除已取消）的挂号数量
     * @param start 开始时间（含）
     * @param end 结束时间（不含）
     * @return 挂号数量
     */
    Long countTodayRegistrations(LocalDateTime start, LocalDateTime end);

    /**
     * 统计处于指定状态集合内的挂号数量
     * @param statuses 状态集合
     * @return 挂号数量
     */
    Long countByStatuses(Collection<Integer> statuses);

    /**
     * 统计处于指定状态的挂号数量
     * @param status 状态
     * @return 挂号数量
     */
    Long countByStatus(Integer status);

    /**
     * 按排班ID集合 + 状态集合查询挂号列表（按挂号时间升序）
     * @param scheduleIds 排班ID集合
     * @param statuses 状态集合
     * @return 挂号列表
     */
    List<Registration> listByScheduleIdsAndStatuses(List<Long> scheduleIds, Collection<Integer> statuses);

    /**
     * 按排班ID集合 + 单个状态查询挂号列表（按报到时间升序）
     * @param scheduleIds 排班ID集合
     * @param status 状态
     * @return 挂号列表
     */
    List<Registration> listByScheduleIdsAndStatus(List<Long> scheduleIds, Integer status);

    /**
     * 挂号状态迁移：原子更新 registration.status（带 status=fromStatus 乐观守门）+ 写状态日志
     * @param reg 已加载的挂号实体
     * @param toStatus 目标状态
     * @param operatorId 操作人ID(0=系统)
     * @param operatorRole 操作人角色(user/doctor/pharmacist/system)
     * @param remark 备注
     * @throws BizException 非法状态流转或乐观守门失败时抛出 REGISTRATION_STATUS_INVALID
     */
    void updateStatusWithLog(Registration reg, int toStatus, Long operatorId, String operatorRole, String remark);
}
