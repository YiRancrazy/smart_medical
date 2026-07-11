package com.yirancrazy.smartmedical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.vo.UserBaseInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:11
 * @Version: 1.0
 */

@Mapper
public interface UserMapper extends BaseMapper<User> {


    // 清空整张表
    @Select("TRUNCATE TABLE user")
    void truncateTable();

    @Select("SELECT account_id,uid,nickname,avatar FROM account,user WHERE user.user_id= #{userId} and account.user_id = user.user_id")
    UserBaseInfo getUserBaseInfoByUserId(Long userId);

}
