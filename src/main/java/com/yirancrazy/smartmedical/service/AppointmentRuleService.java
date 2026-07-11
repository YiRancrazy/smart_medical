package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.AppointmentRule;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 预约规则配置服务接口
 * @Datetime: 2026-02-28
 * @Version: 1.0
 */

public interface AppointmentRuleService {

    /**
     * 插入预约规则
     * @param appointmentRule 预约规则
     * @return 插入的行数
     */
    Integer insertAppointmentRule(AppointmentRule appointmentRule);

    /**
     * 根据ID查询预约规则
     * @param id ID
     * @return 预约规则
     */
    AppointmentRule getAppointmentRuleById(Long id);

    /**
     * 根据ID更新预约规则
     * @param appointmentRule 预约规则
     * @return 更新的行数
     */
    Integer updateAppointmentRuleById(AppointmentRule appointmentRule);

    /**
     * 根据ID删除预约规则
     * @param id ID
     * @return 删除的行数
     */
    Integer deleteAppointmentRuleById(Long id);

    /**
     * 列出所有预约规则
     * @return 预约规则列表
     */
    List<AppointmentRule> listAllAppointmentRules();

    /**
     * 分页获取所有预约规则
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页预约规则列表
     */
    PageInfo<AppointmentRule> listAppointmentRulesByPage(Integer pageNum, Integer pageSize);

    /**
     * 根据部门ID获取预约规则
     * @param departmentId 部门ID
     * @return 预约规则列表
     */
    List<AppointmentRule> listAppointmentsRulesByDepartmentId(Long departmentId);

    /**
     * 根据医生ID获取预约规则
     * @param doctorId 医生ID
     * @return 预约规则列表
     */
    List<AppointmentRule> listAppointmentsRulesByDoctorId(Long doctorId);
}
