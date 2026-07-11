package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.service.RegistrationService;
import com.yirancrazy.smartmedical.service.RegistrationStatusLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 挂号状态迁移工具
 * @Author: YiRanCrazy@gmail.com
 * @Description: 把 "update registration.status + 写状态日志" 统一封装，避免散落到各 Manager
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class RegistrationStatusLogManager {

    private final RegistrationService registrationService;
    private final RegistrationStatusLogService registrationStatusLogService;

    /**
     * 状态迁移：原子地更新 registration.status + 写日志，按需填充 check_in_time / visit_start_time / visit_end_time
     * @param reg 已加载的挂号实体
     * @param toStatus 目标状态
     * @param operatorId 操作人ID(0=系统)
     * @param operatorRole 操作人角色(user/doctor/pharmacist/system)
     * @param remark 备注
     * @throws BizException 当 update 返回 0 行（乐观守门失败）抛出 REGISTRATION_STATUS_INVALID
     */
    @Transactional(rollbackFor = Exception.class)
    public void transition(Registration reg, int toStatus,
                           Long operatorId, String operatorRole, String remark) {
        Integer fromStatus = reg.getStatus();
        if (fromStatus == null) {
            fromStatus = -1;
        }

        // 1. 在内存中按需填充时间字段；updateById 默认忽略 null 字段，不会破坏未设置的时间
        LocalDateTime now = LocalDateTime.now();
        reg.setStatus(toStatus);
        if (toStatus == RegistrationStatusEnum.REPORTED.getCode()) {
            reg.setCheckInTime(now);
        } else if (toStatus == RegistrationStatusEnum.IN_TREATMENT.getCode()) {
            reg.setVisitStartTime(now);
        } else if (toStatus == RegistrationStatusEnum.COMPLETED.getCode()) {
            reg.setVisitEndTime(now);
        }
        int rows = registrationService.updateRegistrationById(reg);
        if (rows == 0) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID,
                    "状态更新失败，请刷新");
        }

        // 2. 写状态日志
        registrationStatusLogService.writeLog(reg.getId(), fromStatus, toStatus,
                operatorId, operatorRole, remark);
        log.info("[registration-status] regId={} {}->{} by {}({})",
                reg.getId(), fromStatus, toStatus, operatorRole, operatorId);
    }

    /**
     * 辅助方法：生成雪花 ID（用于某些 Manager 直接调用）
     * @return 下一个雪花 ID
     */
    public Long nextId() {
        return IdUtil.getSnowflakeNextId();
    }
}