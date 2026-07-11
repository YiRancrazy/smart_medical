package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Registration;

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
    Registration getRegistrationByRegistrationScheduleTemplateIdAndUserId(Long registrationScheduleTemplateId, Long userId);

    /**
     * 根据用户id获取挂号信息
     * @param userId 用户id
     * @return 挂号信息
     */
    List<Registration> listRegistrationsByUserId(Long userId);
}
