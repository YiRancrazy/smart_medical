package com.yirancrazy.smartmedical.service;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.pojo.Chat;

import java.util.List;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 聊天服务接口
 * @Datetime: 2026-02-02 13:13
 * @Version: 1.0
 */

public interface ChatService {

    /**
     * 插入聊天记录
     * @param chat 聊天记录
     * @return 插入的聊天记录ID
     */
    Integer insertChat(Chat chat);

    /**
     * 根据ID查询聊天记录
     * @param id 聊天记录ID
     * @return 聊天记录
     */
    Chat getChatById(Long id);

    /**
     * 更新聊天记录
     * @param chat 聊天记录
     * @return 更新的聊天记录ID
     */
    Integer updateChatById(Chat chat);

    /**
     * 删除聊天记录
     * @param id 聊天记录ID
     * @return 删除的聊天记录ID
     */
    Integer deleteChatById(Long id);

    /**
     * 查询所有聊天记录
     * @return 所有聊天记录
     */
    List<Chat> listAllChats();

    /**
     * 分页查询聊天记录
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页的聊天记录
     */
    PageInfo<Chat> listChatsByPage(Integer pageNum, Integer pageSize);

    /**
     * 批量删除聊天记录
     * @param ids 聊天记录ID列表
     * @return 删除的聊天记录ID列表
     */
    Integer deleteBatch(List<Long> ids);

    /**
     * 查询与某用户的所有聊天记录（双向）
     * @param userId1 用户ID1
     * @param userId2 用户ID2
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页的聊天记录
     */
    PageInfo<Chat> listChatsBetweenUsers(Long userId1, Long userId2, Integer pageNum, Integer pageSize);
}
