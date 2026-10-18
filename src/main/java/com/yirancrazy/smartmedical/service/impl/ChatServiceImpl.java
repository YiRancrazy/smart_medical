package com.yirancrazy.smartmedical.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.mapper.ChatMapper;
import com.yirancrazy.smartmedical.pojo.Chat;
import com.yirancrazy.smartmedical.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 聊天服务实现类
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMapper chatMapper;

    /**
     * 插入聊天记录
     * @param chat 聊天记录
     * @return 插入的行数
     */
    @Override
    public Integer insertChat(Chat chat) {
        return chatMapper.insert(chat);
    }

    /**
     * 根据id获取聊天记录
     * @param id 聊天记录id
     * @return 聊天记录
     */
    @Override
    public Chat getChatById(Long id) {
        return chatMapper.selectById(id);
    }

    /**
     * 更新聊天记录
     * @param chat 聊天记录
     * @return 更新的行数
     */
    @Override
    public Integer updateChatById(Chat chat) {
        return chatMapper.updateById(chat);
    }

    /**
     * 删除聊天记录
     * @param id 聊天记录id
     * @return 删除的行数
     */
    @Override
    public Integer deleteChatById(Long id) {
        return chatMapper.deleteById(id);
    }

    /**
     * 获取所有聊天记录
     * @return 聊天记录列表
     */
    @Override
    public List<Chat> listAllChats() {
        return chatMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 分页获取聊天记录
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 聊天记录列表
     */
    @Override
    public PageInfo<Chat> listChatsByPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Chat> chats = chatMapper.selectList(new LambdaQueryWrapper<>());
        return new PageInfo<>(chats);
    }

    /**
     * 批量删除聊天记录
     * @param idList 聊天记录id列表
     * @return 删除的行数
     */
    @Override
    public Integer deleteBatch(List<Long> idList) {
        return chatMapper.deleteByIds(idList);
    }

    /**
     * 查询与某用户的所有聊天记录（双向）
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页的聊天记录
     */
    @Override
    public PageInfo<Chat> listChatsBetweenUsers(Long userId1, Long userId2, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        LambdaQueryWrapper<Chat> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Chat::getSendId, userId1).eq(Chat::getReceiveId, userId2))
                .or(w -> w.eq(Chat::getSendId, userId2).eq(Chat::getReceiveId, userId1))
                .orderByDesc(Chat::getCreateTime);
        List<Chat> chats = chatMapper.selectList(wrapper);
        return new PageInfo<>(chats);
    }
}
