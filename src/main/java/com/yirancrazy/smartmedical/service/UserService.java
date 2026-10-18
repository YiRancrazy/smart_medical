package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.vo.UserBaseInfo;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户服务接口
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

public interface UserService {

    /**
     * 添加用户信息
     * @param user 用户对象
     * @return 添加结果
     */
    Integer insertUser(User user);

    /**
     * 根据ID查询用户信息
     * @param id 用户ID
     * @return 用户对象
     */
    User getUserById(Long id);

    /**
     * 根据ID删除用户
     * @param id 用户ID
     * @return 删除结果
     */
    Integer deleteUserById(Long id);

    /**
     * 根据ID更新用户信息
     * @param user 用户对象
     * @return 更新结果
     */
    Integer updateUserById(User user);

    /**
     * 查询所有用户列表
     * @return 用户列表
     */
    List<User> listAllUsers();

    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 页面大小
     * @return 分页结果
     */
    PageInfo<User> listUsersByPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除用户
     * @param ids 用户ID列表
     * @return 删除结果
     */
    Integer deleteBatch(List<Long> ids);

    /**
     * 获取用户总数
     * @return 用户数量
     */
    Integer getUserCount();

    /**
     * 根据用户ID获取用户基本信息
     * @param userId 用户ID
     * @return 用户基本信息
     */
    UserBaseInfo getUserBaseInfoByUserId(Long userId);

    /**
     * 根据用户ID列表批量查询用户信息
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    List<User> listUsersByUserIds(List<Long> userIds);

    /**
     * 根据身份证号查询用户信息
     * @param idCard 身份证号
     * @return 用户对象
     */
    User getUserByIdCard(String idCard);

    /**
     * 根据用户姓名模糊查询用户ID列表
     * @param nickname 用户姓名（模糊）
     * @return 用户ID列表
     */
    List<Long> listUserIdsByNicknameLike(String nickname);
}
