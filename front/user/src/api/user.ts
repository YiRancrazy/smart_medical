import request from './index'
import type { ApiResult } from './types'

/**
 * 用户信息模块 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户基础信息查询（展示名 / 本人就诊卡卡号）
 * @Datetime: 2026-09-13 16:00
 * @Version: 1.0
 */

export interface UserBaseInfo {
  accountId: string
  userId: string
  nickname: string
  username: string
  avatar: string
  /** 展示名：本人就诊卡姓名 > 账号默认昵称 > "-" */
  displayName: string
  /** 本人就诊卡卡号，无则 null */
  ownPatientCardSn: string | null
}

export interface UserProfile {
  userId: string
  username: string
  nickname: string
  avatar: string
  phone: string
  idCard: string
  sex: number | null
  address: string
  profileCompleted: boolean
}

export interface UpdateUserProfileRequest {
  username: string
  nickname?: string
  idCard: string
  sex: number
  address?: string
}

export interface UserPhoneSmsCodeRequest {
  phone: string
  scene: 'old' | 'new'
}

export interface UpdateUserPhoneRequest {
  oldPhoneCode: string
  newPhone: string
  newPhoneCode: string
}

/**
 * 用户基础信息（含展示名与本人就诊卡卡号）
 */
export function getUserBaseInfo(userId: number | string) {
  return request.get<any, ApiResult<UserBaseInfo>>(
    '/api/user/v1/user/baseinfo',
    { params: { userId } }
  )
}

/**
 * 获取当前登录用户个人信息
 */
export function getUserProfile() {
  return request.get<any, ApiResult<UserProfile>>('/api/user/v1/user/profile')
}

/**
 * 完善当前登录用户个人信息
 */
export function updateUserProfile(data: UpdateUserProfileRequest) {
  return request.put<any, ApiResult<UserProfile>>('/api/user/v1/user/profile', data)
}

/**
 * 上传并更新当前用户头像
 */
export function uploadUserAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, ApiResult<string>>('/api/user/v1/user/profile/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 发送手机号换绑验证码
 */
export function sendPhoneChangeCode(data: UserPhoneSmsCodeRequest) {
  return request.post<any, ApiResult<string>>('/api/user/v1/user/profile/phone/sms-code', data)
}

/**
 * 校验旧号和新号验证码并换绑手机号
 */
export function changePhone(data: UpdateUserPhoneRequest) {
  return request.put<any, ApiResult<string>>('/api/user/v1/user/profile/phone', data)
}
