package com.yirancrazy.smartmedical.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yirancrazy.smartmedical.pojo.RegistrationStatusLog;

/**
 * 挂号状态日志 Service
 * @Author: YiRanCrazy@gmail.com
 * @Description: 状态变更日志读写
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

public interface RegistrationStatusLogService extends IService<RegistrationStatusLog> {

    /**
     * 写入一条状态变更日志
     * @param registrationId 挂号记录ID
     * @param fromStatus 变更前状态
     * @param toStatus 变更后状态
     * @param operatorId 操作人ID(0=系统)
     * @param operatorRole 操作人角色(user/doctor/pharmacist/system)
     * @param remark 变更备注/原因
     */
    void writeLog(Long registrationId, Integer fromStatus, Integer toStatus,
                  Long operatorId, String operatorRole, String remark);
}