package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.Role;
import com.yirancrazy.smartmedical.service.RoleService;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-03-06 11:30
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class RoleManager {
    private final RoleService roleService;

    /**
     * 根据ID查询角色
     * @param username 用户名
     * @param name 角色名称
     * @param remark 角色描述
     * @return 角色信息
     */
    public Result<Role> insertRole(String username,String name,String remark) {
        Role role = new Role();
        role.setId(Long.valueOf(IdUtil.getSnowflakeNextIdStr()));
        role.setName(name);
        role.setRemark(remark);
        Integer result = roleService.insertRole(role);
        if (result <= 0) {
            return Result.fail("添加角色失败");
        }
        return Result.success(role);
    }

    /**
     * 根据ID删除角色
     * @param id 角色ID
     * @return 删除结果
     */
    public Result<Integer> deleteRoleById(Long id) {
        Integer result = roleService.deleteRoleById(id);
        if (result <= 0) {
            return Result.fail("删除角色失败");
        }
        return Result.success(result);
    }

    /**
     * 根据ID更新角色
     * @param id 角色ID
     * @param name 角色名称
     * @param remark 角色描述
     * @return 角色信息
     */
    public Result<Role> updateRoleById(Long id,String name,String remark) {
        Role role = new Role();
        role.setId(id);
        role.setName(name);
        role.setRemark(remark);
        Integer result = roleService.updateRoleById(role);
        if (result <= 0) {
            return Result.fail("更新角色失败");
        }
        return Result.success(role);
    }

    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色信息
     */
    public Result<Role> getRoleById(Long id) {
        Role role = roleService.getRoleById(id);
        if (role == null) {
            return Result.fail("角色不存在");
        }
        return Result.success(role);
    }

    /**
     * 查询所有角色
     * @return 角色列表
     */
    public Result<List<Role>> listAllRoles() {
        List<Role> roles = roleService.listAllRoles();
        return Result.success(roles);
    }
}