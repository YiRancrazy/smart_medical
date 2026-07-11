package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Chat;
import com.yirancrazy.smartmedical.service.ChatService;
import lombok.RequiredArgsConstructor;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:15
 * @Version: 1.0
 */

@Manager
@RequiredArgsConstructor
public class ChatManager {

    private final ChatService chatService;

    public int addChat(Chat chat) {
        chat.setId(IdUtil.getSnowflakeNextId());
        return chatService.insertChat(chat);
    }

    public Chat getChatById(Long id) {
        return chatService.getChatById(id);
    }
}
