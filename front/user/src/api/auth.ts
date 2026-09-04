import axios from 'axios'
import request from './index'
import type { ApiResult, LoginVo } from './types'
import { getDeviceId } from '@/utils/captcha'

const baseURL = import.meta.env.VITE_API_BASE_URL || ''

function isValidJwt(token: string): boolean {
  return typeof token === 'string' && token.split('.').length === 3
}

/**
 * 用户登录（优先从响应头提取token，兼容响应体）
 */
export const authApi = {
  login: async (data: { phone: string; password: string }): Promise<ApiResult<LoginVo>> => {
    const response = await axios.post(`${baseURL}/api/user/v1/auth/login`, data, {
      withCredentials: true, // 允许读取响应头和发送Cookie
      headers: { 'X-Device-Id': getDeviceId() }
    })

    const resData = response.data as ApiResult<LoginVo>

    // 优先从响应头提取token（统一方案），兼容响应体中的token字段
    const headerToken = response.headers.authorization?.replace('Bearer ', '')
    const bodyToken = resData.data?.token

    const token = headerToken || bodyToken

    if (!token) {
      throw new Error('登录失败：未获取到token')
    }

    if (!isValidJwt(token)) {
      throw new Error(`登录失败：服务器返回的token格式异常`)
    }

    // 确保token注入到响应数据中
    if (resData.data) {
      resData.data.token = token
    }

    return resData
  },
  register: (data: { phone: string; password: string }) =>
    request.post<any, ApiResult<string>>('/api/user/v1/auth/register', data, {
      headers: { 'X-Device-Id': getDeviceId() }
    }),
  // U18: logout 用裸 axios 跳过业务码拦截器，避免后端 500 时弹"操作失败"与登出成功矛盾
  logout: async (): Promise<ApiResult<string>> => {
    const token = localStorage.getItem('user_access_token')
    const res = await axios.post(`${baseURL}/api/user/v1/auth/logout`, {}, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    return res.data as ApiResult<string>
  }
}
