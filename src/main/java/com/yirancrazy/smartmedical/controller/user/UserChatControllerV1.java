package com.yirancrazy.smartmedical.controller.user;

import com.yirancrazy.smartmedical.manager.ChatManager;
import com.yirancrazy.smartmedical.pojo.Chat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description:
 * @Datetime: 2026-02-02 13:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/chat")
@RequiredArgsConstructor
@Tag(name = "聊天记录管理", description = "聊天记录相关接口")
public class UserChatControllerV1 {

    private final ChatManager chatManager;
    @PostMapping("/add")
    @Operation(summary = "添加聊天记录", description = "添加新聊天记录")
    public int addChat(@RequestBody Chat chat) {
        return chatManager.addChat(chat);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取聊天记录", description = "根据聊天记录ID获取聊天记录")
    @Parameter(name = "id", description = "聊天记录ID", required = true)
    public Chat getChatById(@PathVariable String id) {
        return chatManager.getChatById(Long.parseLong(id));
    }
}
