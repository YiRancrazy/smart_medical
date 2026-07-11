package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.AppointmentBlacklist;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 预约黑名单服务接口
 * @Datetime: 2026-02-28
 * @Version: 1.0
 */

public interface AppointmentBlacklistService {

    /**
     * 新增预约黑名单
     * @param appointmentBlacklist 预约黑名单
     * @return 新增的预约黑名单ID
     */
    Integer insertAppointmentBlacklist(AppointmentBlacklist appointmentBlacklist);

    /**
     * 根据ID查询预约黑名单
     * @param id 预约黑名单id
     * @return 预约黑名单
     */
    AppointmentBlacklist getAppointmentBlacklistById(Long id);

    /**
     * 根据ID更新预约黑名单
     * @param appointmentBlacklist 预约黑名单
     * @return 更新的行数
     */
    Integer updateAppointmentBlacklistById(AppointmentBlacklist appointmentBlacklist);

    /**
     * 根据ID删除预约黑名单
     * @param id 预约黑名单id
     * @return 删除的行数
     */
    Integer deleteAppointmentBlacklistById(Long id);

    /**
     * 查询所有预约黑名单
     * @return 预约黑名单列表
     */
    List<AppointmentBlacklist> listAllAppointmentBlacklists();

    /**
     * 分页查询预约黑名单
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 预约黑名单列表
     */
    PageInfo<AppointmentBlacklist> listAppointmentBlacklistsByPage(Integer pageNum, Integer pageSize);
}
