package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.AdminMapper;
import com.yirancrazy.smartmedical.pojo.Admin;
import com.yirancrazy.smartmedical.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员服务实现类
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;

    /**
     * 添加管理员
     * @param admin 管理员对象
     * @return 添加结果
     */
    @Override
    public Integer insertAdmin(Admin admin) {
        return adminMapper.insert(admin);
    }

    /**
     * 根据ID查询管理员
     * @param id 管理员ID
     * @return 管理员对象
     */
    @Override
    public Admin getAdminById(Long id) {
        return adminMapper.selectById(id);
    }

    /**
     * 根据ID更新管理员
     * @param admin 管理员对象
     * @return 更新结果
     */
    @Override
    public Integer updateAdminById(Admin admin) {
        return adminMapper.updateById(admin);
    }

    /**
     * 根据ID删除管理员
     * @param id 管理员ID
     * @return 删除结果
     */
    @Override
    public Integer deleteAdminById(Long id) {
        return adminMapper.deleteById(id);
    }

    /**
     * 获取所有管理员
     * @return 管理员列表
     */
    @Override
    public List<Admin> listAdmins() {
        return adminMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 分页查询管理员
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页管理员列表
     */
    @Override
    public PageInfo<Admin> listAdminsByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> admins = adminMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(admins);
    }

    /**
     * 批量删除管理员
     * @param ids 管理员ID列表
     * @return 删除结果
     */
    @Override
    public Integer deleteAdminByIds(List<Long> ids) {
        return adminMapper.deleteByIds(ids);
    }

    /**
     * 根据ID列表查询管理员
     * @param ids 管理员ID列表
     * @return 管理员列表
     */
    @Override
    public List<Admin> listAdminsByIds(List<Long> ids) {
        return adminMapper.selectList(new LambdaQueryWrapper<Admin>().in(Admin::getId, ids));
    }

    /**
     * 根据名称模糊查询管理员
     * @param name 管理员名称（可为空）
     * @return 管理员列表
     */
    @Override
    public List<Admin> listAdminsByLikeName(String name) {
        return adminMapper.selectList(new LambdaQueryWrapper<Admin>()
                .like(name != null && !name.isEmpty(), Admin::getName, name));
    }
}
