package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Account;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.dto.user.request.UpdateUserProfileRequest;
import com.yirancrazy.smartmedical.pojo.vo.UserBaseInfo;
import com.yirancrazy.smartmedical.pojo.vo.UserInfoVo;
import com.yirancrazy.smartmedical.pojo.vo.UserProfileInfo;
import com.yirancrazy.smartmedical.service.AccountService;
import com.yirancrazy.smartmedical.service.UserService;
import com.yirancrazy.smartmedical.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户业务编排：用户 / 就诊人基础信息查询
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Slf4j
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
    public Result<UserInfoVo> getUserDetailsById(Long accountId) {
        Account account = accountService.getAccountById(accountId);
        User user = userService.getUserById(account.getUserId());

        // 组装返回元素
        UserInfoVo userDetails = new UserInfoVo();
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
        log.info("获取用户基础信息: {}", userBaseInfo);
        return Result.success(userBaseInfo);
    }

    /**
     * 获取当前登录用户的个人信息
     * @param userId 当前登录用户ID
     * @return 用户个人信息
     */
    public Result<UserProfileInfo> getUserProfile(Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.info(10001, "用户不存在", null);
        }
        return Result.success(toUserProfileInfo(user));
    }

    /**
     * 更新当前登录用户的个人信息并返回最新完善状态
     * @param userId 当前登录用户ID
     * @param request 个人信息请求
     * @return 更新后的用户个人信息
     */
    @Transactional(rollbackFor = Exception.class)
    public Result<UserProfileInfo> updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.info(10001, "用户不存在", null);
        }
        user.setUsername(request.getUsername().trim());
        user.setNickname(request.getUsername().trim());
        user.setIdCard(request.getIdCard().trim().toUpperCase(Locale.ROOT));
        user.setSex(request.getSex());
        user.setAddress(StringUtils.hasText(request.getAddress()) ? request.getAddress().trim() : null);
        userService.updateUserById(user);
        return Result.success(toUserProfileInfo(user));
    }

    /**
     * 组装用户个人信息并计算完善状态
     */
    private UserProfileInfo toUserProfileInfo(User user) {
        boolean profileCompleted = StringUtils.hasText(user.getUsername())
                && StringUtils.hasText(user.getIdCard())
                && user.getSex() != null;
        return new UserProfileInfo(
                String.valueOf(user.getId()),
                user.getUsername(),
                user.getAvatar(),
                user.getIdCard(),
                user.getSex(),
                user.getAddress(),
                profileCompleted);
    }
}
