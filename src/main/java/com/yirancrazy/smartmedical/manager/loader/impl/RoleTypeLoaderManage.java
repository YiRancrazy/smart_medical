package com.yirancrazy.smartmedical.manager.loader.impl;

import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Role;
import com.yirancrazy.smartmedical.service.RoleService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public class RoleTypeLoaderManage {

    private final RoleService roleService;

    /**
     * 应用启动时从 role 表加载全部角色到内存常量 ROLE_LIST，供鉴权 / 角色映射复用
     */
    @PostConstruct
    public void loadRoleType() {
        List<Role> roles = roleService.listAllRoles();
        ROLE_LIST.addAll(roles);
        log.info("[loader] 角色数据加载完成，共{}个", roles.size());
    }
}