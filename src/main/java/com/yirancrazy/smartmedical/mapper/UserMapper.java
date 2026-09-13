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


    /**
     * 查询用户基础信息（含展示名与本人就诊卡卡号）
     * <p>展示名规则：存在 relation='本人' 且 patient_user_id=自身 的绑定关系时优先取真实姓名 user.username，
     * 否则取账号默认昵称 user.nickname，两者皆空返回 "-"；本人卡号取该用户 patient 关联的就诊卡 sn，无则 null</p>
     * @param userId 用户ID
     * @return 用户基础信息
     */
    @Select("SELECT CAST(account.id AS CHAR) AS accountId, "
            + "CAST(user.id AS CHAR) AS userId, "
            + "user.nickname, user.username, user.avatar, "
            + "CASE WHEN EXISTS (SELECT 1 FROM user_patient_relation r "
            + "                  WHERE r.user_id = user.id AND r.patient_user_id = user.id "
            + "                  AND r.relation = '本人' AND r.is_deleted = 0) "
            + "     THEN COALESCE(NULLIF(user.username, ''), NULLIF(user.nickname, ''), '-') "
            + "     ELSE COALESCE(NULLIF(user.nickname, ''), '-') END AS displayName, "
            + "(SELECT CAST(pc.sn AS CHAR) FROM patient p "
            + " JOIN patient_card pc ON pc.id = p.patient_card_id "
            + " WHERE p.user_id = user.id AND p.is_deleted = 0 AND pc.is_deleted = 0 LIMIT 1) AS ownPatientCardSn "
            + "FROM account, user "
            + "WHERE user.id = #{userId} AND user.is_deleted = 0 AND account.user_id = user.id AND account.is_deleted = 0")
    UserBaseInfo getUserBaseInfoByUserId(Long userId);

}
