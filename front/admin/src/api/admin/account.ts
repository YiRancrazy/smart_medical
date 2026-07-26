import request from '../index'
import type { Result, PageInfo } from '../types'

/**
 * 账户管理 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员账户的分页查询
 * @Datetime: 2026-07-17 10:05
 * @Version: 1.0
 */

/** 后端 AdminAdminSimpleResponse 字段 */
export interface AccountDetailResponse {
  id: string
  username: string
  phone: string
  avatar: string
  remark: string
  departmentId: string
  departmentName: string
  role: string
  roleId: string
  enabled: boolean
  email: string
}

export interface AccountConditionsParams {
  username?: string
  roleId?: number
  pageNum?: number
  pageSize?: number
}

/**
 * 账户分页查询（多条件）
 * 注意：返回 PageInfo（PageHelper 结构）
 */
export function listAccountsByConditions(params: AccountConditionsParams) {
  return request.get<any, Result<PageInfo<AccountDetailResponse>>>('/api/admin/v1/accounts/detail', { params })
}

/**
 * 更新账户（F20）
 */
export function updateAccount(accountId: string | number, params: {
  roleId?: number
  enabled?: boolean
  phone?: string
}) {
  return request.put<any, Result<void>>(`/api/admin/v1/accounts/${accountId}`, params)
}

/**
 * 删除账户（F20，软删除）
 */
export function deleteAccount(accountId: string | number) {
  return request.delete<any, Result<void>>(`/api/admin/v1/accounts/${accountId}`)
}
