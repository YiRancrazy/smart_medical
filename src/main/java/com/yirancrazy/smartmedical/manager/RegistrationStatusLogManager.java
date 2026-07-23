package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.constant.RegistrationStatusEnum;
import com.yirancrazy.smartmedical.exception.BizErrorCode;
import com.yirancrazy.smartmedical.exception.BizException;
import com.yirancrazy.smartmedical.mapper.RegistrationMapper;
import com.yirancrazy.smartmedical.pojo.Registration;
import com.yirancrazy.smartmedical.service.RegistrationStatusLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 挂号状态迁移工具
 * @Author: YiRanCrazy@gmail.com
 * @Description: 把 "update registration.status + 写状态日志" 统一封装，避免散落到各 Manager；
 *              使用 UpdateWrapper 添加 WHERE status = fromStatus 乐观守门，防止并发状态变更被静默覆盖。
 *              注：使用 UpdateWrapper（字符串列名）而非 LambdaUpdateWrapper，避免单元测试中 MyBatis-Plus lambda cache 未初始化的限制。
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Slf4j
@Manager
@RequiredArgsConstructor
public class RegistrationStatusLogManager {

    private final RegistrationStatusLogService registrationStatusLogService;
    private final RegistrationMapper registrationMapper;

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

        // 0. 业务白名单校验：禁止任意状态互转
        Set<Integer> allowedTo = ALLOWED_TRANSITIONS.get(fromStatus);
        if (allowedTo == null || !allowedTo.contains(toStatus)) {
            throw new BizException(BizErrorCode.REGISTRATION_STATUS_INVALID,
                    "非法状态流转：" + fromStatus + " -> " + toStatus);
        }

        // 1. 构造带乐观守门的 UPDATE：WHERE id=? AND status=fromStatus
        //    使用 UpdateWrapper(字符串列名)而非 LambdaUpdateWrapper(实体方法引用)，
        //    后者依赖 MyBatis-Plus lambda cache，单元测试场景下未初始化会抛 MybatisPlusException。
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