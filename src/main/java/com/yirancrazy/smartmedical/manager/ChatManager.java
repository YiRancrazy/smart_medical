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
     * @throws BizException 文件类型/大小不合法
     */
    public String uploadImage(MultipartFile file) throws Exception {
        // S23: 校验文件类型与大小
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("仅允许上传图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("图片大小不能超过 5MB");
        }
        // 仅用雪花 ID 作为对象名，避免原始文件名含特殊字符或 ../ 注入 MinIO 路径
        String ext = "";
        String original = file.getOriginalFilename();
        if (original != null) {
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) {
                String raw = original.substring(dot + 1).toLowerCase();
                if (raw.matches("[a-z0-9]{1,8}")) {
                    ext = "." + raw;
                }
            }
        }
        String objectName = "chat/images/" + IdUtil.getSnowflakeNextId() + ext;
        MinIOUtil.uploadFile("imagehost", file, objectName, file.getContentType());
        return MinIOUtil.getBasisUrl() + objectName;
    }
}
