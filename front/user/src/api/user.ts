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

/**
 * 用户基础信息（含展示名与本人就诊卡卡号）
 */
export function getUserBaseInfo(userId: number | string) {
  return request.get<any, ApiResult<UserBaseInfo>>(
    '/api/user/v1/user/baseinfo',
    { params: { userId } }
  )
}
