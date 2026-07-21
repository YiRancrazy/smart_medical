package com.yirancrazy.smartmedical.manager;

import cn.hutool.core.util.IdUtil;
import com.github.pagehelper.PageInfo;
import com.yirancrazy.smartmedical.annotation.Manager;
import com.yirancrazy.smartmedical.pojo.Chat;
import com.yirancrazy.smartmedical.pojo.Result;
import com.yirancrazy.smartmedical.service.ChatService;
import com.yirancrazy.smartmedical.utils.MinIOUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author: YiRanCrazy@gmail.com
 * @Description: 聊天管理器
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

    /**
     * 发送文字消息
     * @param sendId 发送者ID
     * @param receiveId 接收者ID
     * @param content 文字内容
     * @return 聊天记录
     */
    public Chat sendTextMessage(Long sendId, Long receiveId, String content) {
        Chat chat = new Chat();
        chat.setId(IdUtil.getSnowflakeNextId());
        chat.setSendId(sendId);
        chat.setReceiveId(receiveId);
        chat.setContentType(0);
        chat.setContent(content);
        chatService.insertChat(chat);
        return chat;
    }

    /**
     * 发送图片消息
     * @param sendId 发送者ID
     * @param receiveId 接收者ID
     * @param imageUrl 图片URL
     * @return 聊天记录
     */
    public Chat sendImageMessage(Long sendId, Long receiveId, String imageUrl) {
        Chat chat = new Chat();
        chat.setId(IdUtil.getSnowflakeNextId());
        chat.setSendId(sendId);
        chat.setReceiveId(receiveId);
        chat.setContentType(1);
        chat.setContent(imageUrl);
        chatService.insertChat(chat);
        return chat;
    }

    /**
     * 查询与某医生的聊天历史
     * @param userId 用户ID
     * @param doctorId 医生ID
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页的聊天记录
     */
    public Result<PageInfo<Chat>> listChatHistory(Long userId, Long doctorId, Integer pageNum, Integer pageSize) {
        PageInfo<Chat> pageInfo = chatService.listChatsBetweenUsers(userId, doctorId, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 上传图片到MinIO
     * @param file 图片文件
     * @return 图片URL
     */
    public String uploadImage(MultipartFile file) throws Exception {
        String objectName = "chat/images/" + IdUtil.getSnowflakeNextId() + "_" + file.getOriginalFilename();
        MinIOUtil.uploadFile("imagehost", file, objectName, file.getContentType());
        return MinIOUtil.getBasisUrl() + objectName;
    }
}
