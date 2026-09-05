package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.AccountMapper;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Admin;
import com.yirancrazy.smartmedical.pojo.Doctor;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.AdminService;
import com.yirancrazy.smartmedical.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 账户服务实现类
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final AdminService adminService;
    private final DoctorService doctorService;

    /**
     * 添加账户
     * @param account 账户对象
     * @return 添加结果
     */
    @Override
    public int insertAccount(Account account) {
        return accountMapper.insert(account);
    }

    /**
     * 根据ID查询账户
     * @param id 账户ID
     * @return 账户对象
     */
    @Override
    public Account getAccountById(Long id) {
        return accountMapper.selectById(id);
    }

    /**
     * 根据手机号查询账户
     * @param phone 手机号
     * @return 账户对象
     */
    @Override
    public List<Account> getAccountByPhone(String phone) {
        return accountMapper.selectList(new QueryWrapper<Account>().eq("phone", phone));
    }

    /**
     * 获取账户数量
     * @return 账户数量
     */
    @Override
    public long getAccountCount() {
        return accountMapper.selectCount(null);
    }

    /**
     * 更新账户
     * @param account 账户对象
     * @return 更新结果
     */
    @Override
    public int updateAccountById(Account account) {
        return accountMapper.updateById(account);
    }

    /**
     * 根据ID删除账户
     * @param id 账户ID
     * @return 删除结果
     */
    @Override
    public int deleteAccountById(Long id) {
        return accountMapper.deleteById(id);
    }

    /**
     * 分页查询账户
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Account> getAccountPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Account> accounts = accountMapper.selectList(null);
        return new PageInfo<>(accounts);
    }

    /**
     * 批量删除账户
     * @param ids 账户ID列表
     * @return 删除结果
     */
    @Override
    public int deleteAccountsByIds(List<Long> ids) {
        return accountMapper.deleteByIds(ids);
    }

    /**
     * 根据用户ID查询账户
     * @param userId 用户ID
     * @return 账户对象
     */
    @Override
    public Account getAccountByUserId(Long userId) {
        List<Account> accounts = accountMapper.selectList(
                new QueryWrapper<Account>()
                        .eq("user_id", userId)
                        .last("LIMIT 1"));
        return accounts.isEmpty() ? null : accounts.get(0);
    }

    /**
     * 根据用户ID列表查询账户列表
     * @param userIdList 用户ID列表
     * @return 账户列表
     */
    @Override
    public List<Account> listAccountsByUserIds(List<Long> userIdList) {
        return accountMapper.selectList(new QueryWrapper<Account>().in("user_id", userIdList));
    }

    @Override
    public List<Account> listAllAccounts() {
        return accountMapper.selectList(null);
    }

    /**
     * 根据用户名、角色ID、是否启用分页查询账户
     * <p>用户名（admin.name / doctor.name 模糊匹配，account 表无姓名字段，需先解析出 userId 再查账户）</p>
     * @param username 用户名（模糊查询，可为空）
     * @param roleId   角色ID（精确匹配，可为空）
     * @param enabled  是否启用（精确匹配，可为空）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页账户列表
     */
    @Override
    public PageInfo<Account> listAllAccountsByRoleIdAndEnabledAndPage(String username, Long roleId, Boolean enabled, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        if (username != null && !username.isEmpty()) {
            // 账户表无姓名，先按管理员/医生姓名模糊查询出 userId 集合，再过滤 account
            List<Long> userIds = new ArrayList<>();
            userIds.addAll(adminService.listAdminsByLikeName(username).stream().map(Admin::getId).toList());
            userIds.addAll(doctorService
                    .listDoctorsSimpleResponseByLikeDoctorNameAndDepartmentId(username, null)
                    .stream().map(Doctor::getId).toList());
            if (userIds.isEmpty()) {
                // 无匹配姓名：清空 PageHelper 上下文，直接返回空页，避免污染同线程后续查询
                PageHelper.clearPage();
                return new PageInfo<>(List.of());
            }
            wrapper.in(Account::getUserId, userIds);
        }
        if (roleId != null) {
            wrapper.eq(Account::getRoleId, roleId);
        }
        if (enabled != null) {
            wrapper.eq(Account::getEnabled, enabled);
        }
        PageHelper.startPage(pageNum, pageSize);
        return new PageInfo<>(accountMapper.selectList(wrapper));
    }

    /**
     * 根据ID列表查询账户列表
     * @param ids ID列表
     * @return 账户列表
     */
    @Override
    public List<Account> listAdminsByIds(List<Long> ids) {
        return accountMapper.selectByIds(ids);
    }

    /**
     * 根据用户ID列表查询账户列表
     * @param userIds 用户ID列表
     * @return 账户列表
     */
    @Override
    public List<Account> listAdminByUserIds(List<Long> userIds) {
        return accountMapper.selectList(new LambdaQueryWrapper<Account>().in(Account::getUserId,userIds));
    }
}
