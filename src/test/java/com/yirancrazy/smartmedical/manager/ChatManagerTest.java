package com.yirancrazy.smartmedical.manager;

import com.yirancrazy.smartmedical.pojo.Chat;
import com.yirancrazy.smartmedical.service.ChatService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ChatManager 单测
 */
@ExtendWith(MockitoExtension.class)
class ChatManagerTest {

    @Mock
    private ChatService chatService;

    @InjectMocks
    private ChatManager chatManager;

    @Test
    void addChat_setsSnowflakeIdAndDelegates() {
        Chat chat = new Chat();
        when(chatService.insertChat(any(Chat.class))).thenReturn(1);

        int rows = chatManager.addChat(chat);

        assertEquals(1, rows);
        assertNotNull(chat.getId(), "Chat id should be assigned by ChatManager");
        verify(chatService).insertChat(chat);
    }

    @Test
    void getChatById_delegatesToService() {
        Chat chat = new Chat();
        chat.setId(99L);
        when(chatService.getChatById(99L)).thenReturn(chat);

        Chat result = chatManager.getChatById(99L);

        assertEquals(99L, result.getId());
        verify(chatService).getChatById(99L);
    }
}
