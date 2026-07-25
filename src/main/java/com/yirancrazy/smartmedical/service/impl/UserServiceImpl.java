package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.UserMapper;
import com.yirancrazy.smartmedical.pojo.User;
import com.yirancrazy.smartmedical.pojo.vo.UserBaseInfo;
import com.yirancrazy.smartmedical.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户服务实现类
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertUser(User user) {
        return userMapper.insert(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deleteUserById(Long id) {
        return userMapper.deleteById(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer updateUserById(User user) {
        return userMapper.updateById(user);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> listAllUsers() {
        return userMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageInfo<User> listUsersByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(users);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer deleteBatch(List<Long> ids) {
        return userMapper.deleteBatchIds(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getUserCount() {
        return Math.toIntExact(userMapper.selectCount(new LambdaQueryWrapper<>()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserBaseInfo getUserBaseInfoByUserId(Long userId) {
        return userMapper.getUserBaseInfoByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<User> listUsersByUserIds(List<Long> userIds) {
        return userMapper.selectByIds(userIds);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public User getUserByIdCard(String idCard) {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getIdCard, idCard)
                        .last("LIMIT 1"));
        return users.isEmpty() ? null : users.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> listUserIdsByNicknameLike(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .like(User::getNickname, nickname.trim()))
                .stream()
                .map(User::getId)
                .toList();
    }
}
