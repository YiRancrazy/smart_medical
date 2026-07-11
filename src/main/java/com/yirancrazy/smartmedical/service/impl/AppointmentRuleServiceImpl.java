package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.AppointmentRuleMapper;
import com.yirancrazy.smartmedical.pojo.AppointmentRule;
import com.yirancrazy.smartmedical.service.AppointmentRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 预约规则配置服务实现类
 * @Datetime: 2026-02-28
 * @Version: 1.0
 */

@Service
public class AppointmentRuleServiceImpl implements AppointmentRuleService {

    @Autowired
    private AppointmentRuleMapper appointmentRuleMapper;

    /**
     * 插入预约规则
     * @param appointmentRule 预约规则
     * @return 插入结果
     */
    @Override
    public Integer insertAppointmentRule(AppointmentRule appointmentRule) {
        return appointmentRuleMapper.insert(appointmentRule);
    }

    /**
     * 根据id获取预约规则
     * @param id 预约规则id
     * @return 预约规则
     */
    @Override
    public AppointmentRule getAppointmentRuleById(Long id) {
        return appointmentRuleMapper.selectById(id);
    }

    /**
     * 根据部门ID获取预约规则列表
     * @param departmentId 部门ID
     * @return 预约规则列表
     */
    @Override
    public List<AppointmentRule> listAppointmentsRulesByDepartmentId(Long departmentId) {
        return appointmentRuleMapper.selectList(new LambdaQueryWrapper<AppointmentRule>()
                .eq(AppointmentRule::getDepartmentId, departmentId)
                .orderBy(true,true,AppointmentRule::getPriority));
    }

    /**
     * 根据医生ID获取预约规则列表
     * @param doctorId 医生ID
     * @return 预约规则列表
     */
    @Override
    public List<AppointmentRule> listAppointmentsRulesByDoctorId(Long doctorId) {
        return appointmentRuleMapper.selectList(new LambdaQueryWrapper<AppointmentRule>()
                .eq(AppointmentRule::getDoctorId, doctorId)
                .orderBy(true,true,AppointmentRule::getPriority));
    }

    /**
     * 根据id更新预约规则
     * @param appointmentRule 预约规则
     * @return 更新结果
     */
    @Override
    public Integer updateAppointmentRuleById(AppointmentRule appointmentRule) {
        return appointmentRuleMapper.updateById(appointmentRule);
    }

    /**
     * 根据id删除预约规则
     * @param id 预约规则id
     * @return 删除结果
     */
    @Override
    public Integer deleteAppointmentRuleById(Long id) {
        return appointmentRuleMapper.deleteById(id);
    }

    /**
     * 获取所有预约规则
     * @return 预约规则列表
     */
    @Override
    public List<AppointmentRule> listAllAppointmentRules() {
        return appointmentRuleMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 分页获取所有预约规则
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 预约规则列表
     */
    @Override
    public PageInfo<AppointmentRule> listAppointmentRulesByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<AppointmentRule> appointmentRules = appointmentRuleMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(appointmentRules);
    }
}
