package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.AppointmentBlacklistMapper;
import com.yirancrazy.smartmedical.pojo.AppointmentBlacklist;
import com.yirancrazy.smartmedical.service.AppointmentBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 预约黑名单服务实现类
 * @Datetime: 2026-02-28
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class AppointmentBlacklistServiceImpl implements AppointmentBlacklistService {

    private final AppointmentBlacklistMapper appointmentBlacklistMapper;

    /**
     * 插入预约黑名单
     * @param appointmentBlacklist 预约黑名单
     * @return 插入结果
     */
    @Override
    public Integer insertAppointmentBlacklist(AppointmentBlacklist appointmentBlacklist) {
        return appointmentBlacklistMapper.insert(appointmentBlacklist);
    }

    /**
     * 根据id查询预约黑名单
     * @param id id
     * @return 预约黑名单
     */
    @Override
    public AppointmentBlacklist getAppointmentBlacklistById(Long id) {
        return appointmentBlacklistMapper.selectById(id);
    }

    /**
     * 更新预约黑名单
     * @param appointmentBlacklist 预约黑名单
     * @return 更新结果
     */
    @Override
    public Integer updateAppointmentBlacklistById(AppointmentBlacklist appointmentBlacklist) {
        return appointmentBlacklistMapper.updateById(appointmentBlacklist);
    }

    /**
     * 删除预约黑名单
     * @param id id
     * @return 删除结果
     */
    @Override
    public Integer deleteAppointmentBlacklistById(Long id) {
        return appointmentBlacklistMapper.deleteById(id);
    }

    /**
     * 列出所有预约黑名单
     * @return 预约黑名单列表
     */
    @Override
    public List<AppointmentBlacklist> listAllAppointmentBlacklists() {
        return appointmentBlacklistMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 分页列出所有预约黑名单
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 预约黑名单列表
     */
    @Override
    public PageInfo<AppointmentBlacklist> listAppointmentBlacklistsByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<AppointmentBlacklist> appointmentBlacklists = appointmentBlacklistMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(appointmentBlacklists);
    }
}
