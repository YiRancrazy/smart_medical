package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Admin;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员服务接口
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

public interface AdminService {

    /**
     * 添加管理员
     * @param admin 管理员对象
     * @return 添加结果
     */
    Integer insertAdmin(Admin admin);

    /**
     * 根据ID查询管理员
     * @param id 管理员ID
     * @return 管理员对象
     */
    Admin getAdminById(Long id);

    /**
     * 更新管理员信息
     * @param admin 管理员对象
     * @return 更新结果
     */
    Integer updateAdminById(Admin admin);

    /**
     * 根据ID删除管理员
     * @param id 管理员ID
     * @return 删除结果
     */
    Integer deleteAdminById(Long id);

    /**
     * 查询所有管理员
     * @return 管理员列表
     */
    List<Admin> listAdmins();

    /**
     * 分页查询管理员
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<Admin> listAdminsByPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除管理员
     * @param ids 管理员ID列表
     * @return 删除结果
     */
    Integer deleteAdminByIds(List<Long> ids);

    /**
     * 根据ID列表查询管理员
     * @param ids 管理员ID列表
     * @return 管理员列表
     */
    List<Admin> listAdminsByIds(List<Long> ids);

    /**
     * 根据名称模糊查询管理员
     * @param name 管理员名称（可为空）
     * @return 管理员列表
     */
    List<Admin> listAdminsByLikeName(String name);
}
