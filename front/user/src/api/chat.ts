import request from './index'
import type { ApiResult, PageInfo } from './types'

/**
 * 在线咨询 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户与医生的聊天：历史记录 + 发送文字/图片
 * @Datetime: 2026-09-05 12:00
 */

export interface ChatRecord {
  id: string
  sendId: string
  receiveId: string
  contentType: number // 0 文字 / 1 图片
  content: string
  createTime: string
}

/**
 * 查询与某医生的聊天历史
 */
export function getChatHistory(doctorId: string | number, pageNum = 1, pageSize = 50) {
  return request.get<any, ApiResult<PageInfo<ChatRecord>>>(`/api/user/v1/chat/history/${doctorId}`, {
    params: { pageNum, pageSize }
  })
}

/**
 * 发送文字消息
 */
export function sendChatText(doctorId: string | number, content: string) {
  return request.post<any, ApiResult<ChatRecord>>('/api/user/v1/chat/send/text', { doctorId, content })
}

/**
 * 上传聊天图片，返回图片 URL
 */
export function uploadChatImage(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, ApiResult<string>>('/api/user/v1/chat/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 发送图片消息
 */
export function sendChatImage(doctorId: string | number, imageUrl: string) {
  return request.post<any, ApiResult<ChatRecord>>('/api/user/v1/chat/send/image', { doctorId, imageUrl })
}