package com.yirancrazy.smartmedical.controller.user;

import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.manager.ChatManager;
import com.yirancrazy.smartmedical.pojo.Chat;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.pojo.dto.user.request.SendImageMessageRequest;
import com.yirancrazy.smartmedical.pojo.dto.user.request.SendTextMessageRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端聊天接口
 * @Datetime: 2026-02-02 13:57
 * @Version: 1.0
 */

@RestController
@RequestMapping("api/user/v1/chat")
@RequiredArgsConstructor
@Tag(name = "用户端 - 聊天管理", description = "用户端聊天相关接口")
public class UserChatControllerV1 {

    private final ChatManager chatManager;

    @PostMapping("/add")
    @Operation(summary = "添加聊天记录", description = "添加新聊天记录")
    public Result<Integer> addChat(@Valid @RequestBody Chat chat,
                                   @RequestAttribute("currentUserId") Long userId) {
        // 强制以当前登录用户为发送者，防止身份伪造
        chat.setSendId(userId);
        return Result.success(chatManager.addChat(chat));
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "根据ID获取聊天记录", description = "根据聊天记录ID获取聊天记录")
    @Parameter(name = "id", description = "聊天记录ID", required = true)
    public Result<Chat> getChatById(@PathVariable Long id,
                                    @RequestAttribute("currentUserId") Long userId) {
        Chat chat = chatManager.getChatById(id);
        if (chat == null) {
            return Result.fail("聊天记录不存在");
        }
        // 仅允许发送者或接收者查看，防止 IDOR
        if (!userId.equals(chat.getSendId()) && !userId.equals(chat.getReceiveId())) {
            return Result.fail("无权查看该聊天记录");
        }
        return Result.success(chat);
    }

    /**
     * 查询与某医生的聊天历史
     */
    @GetMapping("/history/{doctorId:\\d+}")
    @Operation(summary = "查询与医生的聊天历史", description = "用户端 - 查询与某医生的聊天历史记录")
    @Parameter(name = "doctorId", description = "医生ID", required = true)
    public Result<PageInfo<Chat>> listChatHistory(
            @PathVariable Long doctorId,
            @RequestAttribute("currentUserId") Long userId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "20") Integer pageSize) {
        return chatManager.listChatHistory(userId, doctorId, pageNum, pageSize);
    }

    /**
     * 发送文字消息
     */
    @PostMapping("/send/text")
    @Operation(summary = "发送文字消息", description = "用户端 - 发送文字消息给医生")
    public Result<Chat> sendTextMessage(
            @Valid @RequestBody SendTextMessageRequest request,
            @RequestAttribute("currentUserId") Long userId) {
        Chat chat = chatManager.sendTextMessage(userId, request.getDoctorId(), request.getContent());
        return Result.success(chat);
    }

    /**
     * 上传图片
     */
    @PostMapping("/upload/image")
    @Operation(summary = "上传聊天图片", description = "用户端 - 上传聊天图片")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = chatManager.uploadImage(file);
            return Result.success(imageUrl);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("图片上传失败");
        }
    }

    /**
     * 发送图片消息
     */
    @PostMapping("/send/image")
    @Operation(summary = "发送图片消息", description = "用户端 - 发送图片消息给医生")
    public Result<Chat> sendImageMessage(
            @Valid @RequestBody SendImageMessageRequest request,
            @RequestAttribute("currentUserId") Long userId) {
        try {
            Chat chat = chatManager.sendImageMessage(userId, request.getDoctorId(), request.getImageUrl());
            return Result.success(chat);
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        }
    }
}
