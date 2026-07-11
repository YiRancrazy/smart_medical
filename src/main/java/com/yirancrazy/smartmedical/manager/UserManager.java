package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.vo.UserBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.UserDetails;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import lombok.RequiredArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class UserManager {

    private final UserService userService;

    private final AccountService accountService;

    private final RedisUtil redisUtil;

    /**
     * 添加用户
     * @param user 用户
     * @return 添加结果
     */
    public int addUser(User user) {
        user.setId(IdUtil.getSnowflakeNextId());
        return userService.insertUser(user);
    }

    /**
     * 根据id获取用户
     * @param id 用户id
     * @return  用户
     */
    public User getUserById(Long id) {
        return userService.getUserById(id);
    }

    /**
     * 删除用户
     * @param id 用户id
     * @return 删除结果
     */
    public int deleteUserById(Long id) {
        return userService.deleteUserById(id);
    }

    /**
     * 获取用户详情
     * @param accountId 账号id
     * @return 用户详情
     */
    public Result<UserDetails> getUserDetailsById(Long accountId) {
        Account account = accountService.getAccountById(accountId);
        User user = userService.getUserById(account.getUserId());

        // 组装返回元素
        UserDetails userDetails = new UserDetails();
        userDetails.setAccountId(String.valueOf(account.getId()));
        userDetails.setUserId(String.valueOf(user.getId()));
        userDetails.setEmail(account.getEmail());
        userDetails.setPhone(account.getPhone());
        userDetails.setAvatar(user.getAvatar());
        userDetails.setSex(user.getSex());
        userDetails.setNickName(user.getUsername());

        return Result.success(userDetails);
    }

    /**
     * 获取用户基础信息
     * @param userId 账号id
     * @return 用户基础信息
     */
    public Result<UserBaseInfo> getUserBaseInfoByUserId(Long userId) {
        UserBaseInfo userBaseInfo = userService.getUserBaseInfoByUserId(userId);
        System.out.println(userBaseInfo);
        return Result.success(userBaseInfo);
    }
}
