package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.Registration;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:11
 * @Version: 1.0
 */

@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    /**
     * 根据用户 id 查询挂号记录
     * @param userId 用户 id
     * @return 挂号记录列表
     */
    List<Registration> listRegistrationsByUserId(Long userId);

    /**
     * 根据排班 id 与用户 id 查找挂号记录（用于重复下单校验）
     * @param registrationScheduleId 排班 id
     * @param userId 用户 id
     * @return 匹配的挂号记录，没有则返回 null
     */
    Registration getRegistrationByRegistrationScheduleIdAndUserId(Long registrationScheduleId, Long userId);
}
