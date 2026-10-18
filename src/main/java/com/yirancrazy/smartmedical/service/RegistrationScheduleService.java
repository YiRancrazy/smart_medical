package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.RegistrationSchedule;
import com.yirancrazy.smartmedical.pojo.RegistrationScheduleTemplate;

import java.time.LocalDate;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 挂号排班服务接口
 * @Datetime: 2026-02-13
 * @Version: 1.0
 */

public interface RegistrationScheduleService {

    /**
     * 添加挂号排班信息
     * @param registrationSchedule 挂号排班对象
     * @return 添加结果
     */
    Integer insertRegistrationSchedule(RegistrationSchedule registrationSchedule);

    /**
     * 根据ID查询挂号排班信息
     * @param id 排班ID
     * @return 挂号排班对象
     */
    RegistrationSchedule getRegistrationScheduleById(Long id);

    /**
     * 根据ID更新挂号排班信息
     * @param registrationSchedule 挂号排班对象
     * @return 更新结果
     */
    Integer updateRegistrationScheduleById(RegistrationSchedule registrationSchedule);

    /**
     * 根据ID删除挂号排班信息
     * @param id 排班ID
     * @return 删除结果
     */
    Integer deleteRegistrationScheduleById(Long id);

    /**
     * 查询所有挂号排班列表
     * @return 挂号排班列表
     */
    List<RegistrationSchedule> listAllRegistrationSchedules();

    /**
     * 分页查询挂号排班列表
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<RegistrationSchedule> listRegistrationSchedulesByPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除挂号排班
     * @param ids 排班ID列表
     * @return 删除结果
     */
    Integer deleteBatch(List<Long> ids);

    /**
     * 根据医生ID获取挂号排班列表
     * @param doctorId 医生ID
     * @return 挂号排班列表
     */
    List<RegistrationSchedule> getRegistrationSchedulesByDoctorId(Long doctorId);

    /**
     * 根据状态获取挂号排班列表
     * @param status 状态
     * @return 挂号排班列表
     */
    List<RegistrationSchedule> getRegistrationSchedulesByStatus(Integer status);

    /**
     * 获取挂号排班总数
     * @return 总数
     */
    Long getRegistrationScheduleCount();

    /**
     * 根据医生ID列表获取最近的挂号排班列表
     * @param doctorIdList 医生ID列表
     * @return 挂号排班列表
     */
    List<RegistrationScheduleTemplate> getRecentRegistrationListByDoctorIdList(List<Long> doctorIdList);

    /**
     * 获取指定医生上周的挂号排班列表
     * @param doctorId 医生ID
     * @return 挂号排班列表
     */
    List<RegistrationSchedule> getRegistrationScheduleLastWeekByDoctorId(Long doctorId);

    /**
     * 根据医生ID和日期获取挂号排班列表
     * @param doctorId 医生ID
     * @param date 日期
     * @return 挂号排班列表
     */
    List<RegistrationSchedule> getRegistrationSchedulesByDoctorIdAndDate(Long doctorId, LocalDate date);

    /**
     * 根据ID列表批量查询挂号排班信息
     * @param registrationScheduleIdList 排班ID列表
     * @return 挂号排班列表
     */
    List<RegistrationSchedule> listRegistrationSchedulesByIds(List<Long> registrationScheduleIdList);

    /**
     * 根据挂号排班模板ID列表获取挂号排班信息
     * @param registrationScheduleTemplateIdList 挂号排班模板ID列表
     * @return 挂号排班信息
     */
    List<RegistrationSchedule> listRegistrationScheduleByRegistrationScheduleIdList(List<Long> registrationScheduleIdList);

    /**
     * 批量插入挂号排班信息
     * @return 插入数量
     */
    Long insertRegistrationScheduleList(List<RegistrationSchedule> list);

    /**
     * 根据挂号排班模板ID获取挂号排班信息
     * @param id 挂号排班模板ID
     * @return 挂号排班信息列表
     */
    List<RegistrationSchedule> getRegistrationScheduleListByRegistrationScheduleId(Long id);

    /**
     * 根据挂号排班模板ID列表获取挂号排班信息
     * @param idList 挂号排班模板ID列表
     * @return 挂号排班信息列表
     */
    List<RegistrationSchedule> getRegistrationScheduleListByRegistrationScheduleIdList(List<Long> idList);

    /**
     * 根据挂号排班模板ID列表获取挂号排班信息（语义别名）
     * @param templateIdList 挂号排班模板ID列表
     * @return 挂号排班信息列表
     */
    List<RegistrationSchedule> getRegistrationScheduleListByTemplateIdList(List<Long> templateIdList);

    /**
     * 原子扣减号源：WHERE remaining_quota > 0 防止并发超卖；扣减后为 0 则置为已满(2)
     * @param scheduleId 排班ID
     * @return 影响行数（0 表示无号源）
     */
    int deductRemainingQuota(Long scheduleId);

    /**
     * 恢复号源：remaining_quota + 1，已满(2)则恢复为正常(1)
     * @param scheduleId 排班ID
     * @return 影响行数
     */
    int releaseQuota(Long scheduleId);
}
