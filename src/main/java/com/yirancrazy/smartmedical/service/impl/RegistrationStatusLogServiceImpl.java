package com.yirancrazy.smartmedical.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yirancrazy.smartmedical.mapper.RegistrationStatusLogMapper;
import com.yirancrazy.smartmedical.pojo.RegistrationStatusLog;
import com.yirancrazy.smartmedical.service.RegistrationStatusLogService;
import org.springframework.stereotype.Service;

/**
 * 挂号状态日志 Service 实现
 * @Author: YiRanCrazy@gmail.com
 * @Description: 状态日志实现
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Service
public class RegistrationStatusLogServiceImpl
        extends ServiceImpl<RegistrationStatusLogMapper, RegistrationStatusLog>
        implements RegistrationStatusLogService {

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeLog(Long registrationId, Integer fromStatus, Integer toStatus,
                         Long operatorId, String operatorRole, String remark) {
        RegistrationStatusLog statusLog = new RegistrationStatusLog();
        statusLog.setId(IdUtil.getSnowflakeNextId());
        statusLog.setRegistrationId(registrationId);
        statusLog.setFromStatus(fromStatus);
        statusLog.setToStatus(toStatus);
        statusLog.setOperatorId(operatorId);
        statusLog.setOperatorRole(operatorRole);
        statusLog.setRemark(remark);
        save(statusLog);
    }
}