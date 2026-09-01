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


    @Select("SELECT CAST(account.id AS CHAR) AS accountId, "
            + "CAST(user.id AS CHAR) AS userId, "
            + "user.nickname, user.username, user.avatar "
            + "FROM account, user "
            + "WHERE user.id = #{userId} AND account.user_id = user.id")
    UserBaseInfo getUserBaseInfoByUserId(Long userId);

}
