package com.yirancrazy.smartmedical.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Account;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 账户服务接口
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

public interface AccountService {

    /**
     * 添加账户
     * @param account 账户信息
     * @return 添加结果
     */
    int insertAccount(Account account);

    /**
     * 根据ID获取账户信息
     * @param id 账户ID
     * @return 账户信息
     */
    Account getAccountById(Long id);

    /**
     * 根据手机号获取账户信息
     * @param phone 手机号
     * @return 账户信息
     */
    List<Account> getAccountByPhone(String phone);

    /**
     * 获取账户数量
     * @return 账户数量
     */
    long getAccountCount();

    /**
     * 更新账户信息
     * @param account 账户信息
     * @return 更新结果
     */
    int updateAccountById(Account account);

    /**
     * 根据ID删除账户
     * @param id 账户ID
     * @return 删除结果
     */
    int deleteAccountById(Long id);

    /**
     * 分页查询账户
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<Account> getAccountPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除账户
     * @param ids 账户ID列表
     * @return 删除结果
     */
    int deleteAccountsByIds(List<Long> ids);

    /**
     * 根据用户ID获取账户信息
     * @param userId 用户ID
     * @return 账户信息
     */
    Account getAccountByUserId(Long userId);

    /**
     * 根据用户ID列表批量获取账户信息
     * @param userIdList 用户ID列表
     * @return 账户信息列表
     */
    List<Account> listAccountsByUserIds(List<Long> userIdList);

    /**
     * 获取所有账户列表
     * @return 账户列表
     */
    List<Account> listAllAccounts();

    /**
     * 根据用户名、角色ID、是否启用分页查询账户详情
     * @param roleId   角色ID（精确匹配，可为空）
     * @param enabled  是否启用（精确匹配，可为空）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页账户详情列表
     */
    PageInfo<Account> listAllAccountsByRoleIdAndEnabledAndPage(Long roleId, Boolean enabled,Integer pageNum, Integer pageSize);

    /**
     * 根据管理员ID列表批量获取管理员信息
     * @param ids 管理员ID列表
     * @return 管理员信息列表
     */
    List<Account> listAdminsByIds(List<Long> ids);

    /**
     * 根据用户ID列表批量获取管理员信息
     * @param userIds 用户ID列表
     * @return 管理员信息列表
     */
    List<Account> listAdminByUserIds(List<Long> userIds);

}
