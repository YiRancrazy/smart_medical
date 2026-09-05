package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.Account;
import org.apache.ibatis.annotations.Mapper;


/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:11
 * @Version: 1.0
 */

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
    /**
     * 根据用户名、角色ID、是否启用分页查询账户详情
     * @param username 用户名（模糊查询，可为空）
     * @param roleId   角色ID（精确匹配，可为空）
     * @param enabled  是否启用（精确匹配，可为空）
     * @return 账户详情列表
     */
}