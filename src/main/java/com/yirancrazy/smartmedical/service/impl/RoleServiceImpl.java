package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.RoleMapper;
import com.yirancrazy.smartmedical.pojo.Role;
import com.yirancrazy.smartmedical.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 角色服务实现类
 * @Datetime: 2026-02-27
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertRole(Role role) {
        return roleMapper.insert(role);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Role getRoleById(Long id) {
        return roleMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer updateRoleById(Role role) {
        return roleMapper.updateById(role);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deleteRoleById(Long id) {
        return roleMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Role> listAllRoles() {
        return roleMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<Role> listRolesByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(roles);
    }
}
