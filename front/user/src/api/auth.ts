import axios from 'axios'
import request from './index'
import type { ApiResult, LoginVo } from './types'

const baseURL = import.meta.env.VITE_API_BASE_URL || ''

function isValidJwt(token: string): boolean {
  return typeof token === 'string' && token.split('.').length === 3
}

/**
 * 通用登录请求：裸 axios 提交并提取 token（优先响应头，兼容响应体），login/register/loginByCode 共用
 * @param path 登录接口路径（相对 baseURL）
 * @param data 请求体
 */
async function doLogin(path: string, data: object): Promise<ApiResult<LoginVo>> {
  const response = await axios.post(`${baseURL}${path}`, data, {
    withCredentials: true // 允许读取响应头和发送Cookie
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
}

/**
 * 用户登录（优先从响应头提取token，兼容响应体）
 */
export const authApi = {
  login: (data: { phone: string; password: string }): Promise<ApiResult<LoginVo>> =>
    doLogin('/api/user/v1/auth/login', data),
  /**
   * 手机号+验证码登录（登录成功自动签发 token）
   */
  loginByCode: (data: { phone: string; code: string }): Promise<ApiResult<LoginVo>> =>
    doLogin('/api/user/v1/auth/login-by-code', data),
  /**
   * 发送短信验证码（scene=register 注册码 / scene=login 登录码 / scene=forgot 忘记密码码）
   */
  sendSmsCode: (phone: string, scene: 'register' | 'login' | 'forgot' = 'register') =>
    request.post<any, ApiResult<string>>('/api/user/v1/auth/sms-code', { phone, scene }),
  /**
   * 短信验证码注册（注册成功自动登录，token 从响应头提取）
   */
  register: (data: { phone: string; code: string }): Promise<ApiResult<LoginVo>> =>
    doLogin('/api/user/v1/auth/register', data),
  // 忘记密码（未登录，需短信验证码校验，走 request 复用 Result 错误提示）
  forgotPassword: (data: { phone: string; code: string; password: string }) =>
    request.post<any, ApiResult<string>>('/api/user/v1/auth/forgot-password', data),
  // 登录态修改密码
  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    request.post<any, ApiResult<string>>('/api/user/v1/auth/change-password', data),
  // U18: logout 用裸 axios 跳过业务码拦截器，避免后端 500 时弹"操作失败"与登出成功矛盾
  logout: async (): Promise<ApiResult<string>> => {
    const token = localStorage.getItem('user_access_token')
    const res = await axios.post(`${baseURL}/api/user/v1/auth/logout`, {}, {
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    })
    return res.data as ApiResult<string>
  }
}
