package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Role;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 角色服务接口
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

public interface RoleService {

    /**
     * 添加角色信息
     * @param role 角色对象
     * @return 添加结果
     */
    Integer insertRole(Role role);

    /**
     * 根据ID查询角色信息
     * @param id 角色ID
     * @return 角色对象
     */
    Role getRoleById(Long id);

    /**
     * 根据ID更新角色信息
     * @param role 角色对象
     * @return 更新结果
     */
    Integer updateRoleById(Role role);

    /**
     * 根据ID删除角色信息
     * @param id 角色ID
     * @return 删除结果
     */
    Integer deleteRoleById(Long id);

    /**
     * 查询所有角色列表
     * @return 角色列表
     */
    List<Role> listAllRoles();

    /**
     * 分页查询角色列表
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<Role> listRolesByPage(Integer pageNum, Integer pageSize);
}
