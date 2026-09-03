package com.yirancrazy.smartmedical.constant;

import com.yirancrazy.smartmedical.pojo.Role;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 角色常量
 * @Datetime: 2026-03-06 09:03
 * @Version: 1.0
 */


public class RoleConstant {
    /** 系统管理员角色 ID */
    public static final long ROLE_ADMIN_ID = 1L;
    /** 医生角色 ID */
    public static final long ROLE_DOCTOR_ID = 2L;

    public static final List<Role> ROLE_LIST = new ArrayList<>();
}