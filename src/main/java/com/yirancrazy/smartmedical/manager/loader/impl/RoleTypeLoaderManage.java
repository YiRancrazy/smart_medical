package com.yirancrazy.smartmedical.manager.loader.impl;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Role;
import com.yirancrazy.smartmedical.service.RoleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.yirancrazy.smartmedical.constant.RoleConstant.ROLE_LIST;


/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-06 08:38
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class RoleTypeLoaderManage {

    private final RoleService roleService;

    @PostConstruct
    public void loadRoleType() {
        List<Role> roles = roleService.listAllRoles();
        ROLE_LIST.addAll(roles);
        System.out.println("角色数据加载完成，共加载 " + roles.size() + " 个角色");

    }
}