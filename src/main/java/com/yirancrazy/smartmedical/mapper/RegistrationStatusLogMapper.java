package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.RegistrationStatusLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 挂号状态变更日志 Mapper
 * @Author: YiRanCrazy@gmail.com
 * @Description: 继承 MyBatis-Plus BaseMapper，提供挂号状态日志表的 CRUD
 * @Datetime: 2026-07-11 10:00
 * @Version: 1.0
 */

@Mapper
public interface RegistrationStatusLogMapper extends BaseMapper<RegistrationStatusLog> {
}