import axios from 'axios'
import request from '../index'
import type { Result } from '../types'

function isValidJwt(token: string): boolean {
  return typeof token === 'string' && token.split('.').length === 3
}

/**
 * 管理员认证相关 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员登录、获取当前用户信息
 * @Datetime: 2026-07-17 10:00
 * @Version: 1.0
 */

export interface LoginParams {
  phone: string
  password: string
  remember: boolean
}

export interface AdminInfoSimple {
  id: number
  phone: string
  username: string
  roleId: number
  enabled: boolean
}

// F01: 生产环境 VITE_API_BASE_URL='/'，字符串拼接会得到 '//api/...' 被浏览器解析为协议相对URL(host=api)。
// 统一去掉末尾斜杠再做拼接，避免与裸 axios 字符串拼接产生双斜杠。
const baseURL = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080').replace(/\/$/, '')

/**
 * 按角色登录（优先从响应头提取token，兼容响应体）
 */
export async function loginByRole(role: number, data: LoginParams): Promise<Result<string>> {
  const prefix = role === 1 ? 'admin' : role === 2 ? 'doctor' : 'pharmacy'
  const response = await axios.post(`${baseURL}/api/${prefix}/v1/auth/login`, data, {
    withCredentials: true
  })

  const resData = response.data as Result<string>

  const headerToken = response.headers.authorization?.replace('Bearer ', '')
  const bodyToken = typeof resData.data === 'string' && isValidJwt(resData.data) ? resData.data : undefined

  const token = headerToken || bodyToken

  if (!token) {
    throw new Error('登录失败：未获取到token')
  }

  if (!isValidJwt(token)) {
    throw new Error('登录失败：服务器返回的token格式异常')
  }

  resData.data = token
  return resData
}

/**
 * 获取当前登录用户信息（按角色）
 */
export function getCurrentByRole(role: number) {
  const prefix = role === 1 ? 'admin' : role === 2 ? 'doctor' : 'pharmacy'
  return request.get<any, Result<AdminInfoSimple>>(`/api/${prefix}/v1/auth/current`)
}

/**
 * 管理员登录
 * 注意：token 从响应头的 Authorization 字段提取
 */
export function login(data: LoginParams) {
  return request.post<any, Result<string>>('/api/admin/v1/auth/login', data)
}

/**
 * 获取当前管理员信息
 */
export function getCurrentAdmin() {
  return request.get<any, Result<AdminInfoSimple>>('/api/admin/v1/auth/current')
}

/**
 * 按角色登出（F08：医生/药师登出原硬编码 admin 接口导致后端 token 未清）
 */
export function logoutByRole(role: number) {
  const prefix = role === 1 ? 'admin' : role === 2 ? 'doctor' : 'pharmacy'
  return request.post<any, Result<string>>(`/api/${prefix}/v1/auth/logout`)
}

/**
 * 管理员登出（保留旧接口）
 */
export function logout() {
  return request.post<any, Result<string>>('/api/admin/v1/auth/logout')
}
