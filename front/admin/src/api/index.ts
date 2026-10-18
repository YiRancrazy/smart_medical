import axios from 'axios'
import { message } from 'ant-design-vue'
import router from '@/router'
import { useAuthStore } from '@/stores/auth'
import { getBizErrorMessage } from '@/utils/bizCode'
import type { Result } from './types'

const request = axios.create({
  // F15: 与 user 端对齐，dev 走 vite proxy（空串），prod 由 VITE_API_BASE_URL 注入；原硬编码 localhost:8080 在 prod 会产生双斜杠且暴露内网
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000
})

let isRefreshing = false
let failedQueue: Array<{ resolve: (token: string) => void; reject: (err: any) => void }> = []

function processQueue(token: string | null, err: any) {
  failedQueue.forEach(p => {
    if (token) p.resolve(token)
    else p.reject(err)
  })
  failedQueue = []
}

function getRefreshUrl(roleId: number | null): string {
  // F01: dev 环境 VITE_API_BASE_URL 为空，走 vite proxy 同源请求，Cookie 不受 SameSite=Lax 限制；
  // prod 设置具体域名时仍用完整 URL
  // CROSS-06: 使用 Map 映射 roleId，未知角色不 fallback 到 pharmacy
  const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
  const rolePathMap: Record<number, string> = { 1: 'admin', 2: 'doctor', 6: 'pharmacy' }
  const prefix = roleId != null ? rolePathMap[roleId] : null
  if (!prefix) {
    return ''
  }
  const path = `/api/${prefix}/v1/auth/refresh`
  return base ? `${base}${path}` : path
}

async function refreshToken(roleId: number | null): Promise<string> {
  const url = getRefreshUrl(roleId)
  if (!url) {
    throw new Error('未知角色，无法刷新 token')
  }
  const maxRetry = 3
  let lastErr: any
  for (let i = 0; i < maxRetry; i++) {
    try {
      const res = await axios.post(url, {}, { withCredentials: true })
      const newToken = (res.headers.authorization?.replace('Bearer ', '')) ||
          (typeof res.data === 'string' ? res.data : res.data?.data)
      if (newToken) {
        const authStore = useAuthStore()
        authStore.applyToken(newToken)
        return newToken
      }
    } catch (e) {
      lastErr = e
      if (i < maxRetry - 1) await new Promise(r => setTimeout(r, 500))
    }
  }
  throw lastErr
}

request.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  // 上传 FormData 时删除默认 Content-Type，让浏览器自动生成带 boundary 的 multipart 头
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  return config
})

request.interceptors.response.use(
  (response: any) => {
    const data = response.data as Result<any>
    if (data && typeof data.code === 'number') {
      if (data.code === 200) {
        return data as any
      }
      message.error(getBizErrorMessage(data.code, data.message))
      return Promise.reject(data)
    }
    return data as any
  },
  async (error) => {
    const status = error.response?.status
    const errorData = error.response?.data
    const config = error.config
    if (status === 401) {
      const authStore = useAuthStore()
      const token = authStore.token
      // 只有在有 token 且不是公开接口时才尝试刷新
      const isPublicApi = config.url?.includes('/auth/login') ||
                          config.url?.includes('/auth/refresh')

      if (token && !isPublicApi) {
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({
              resolve: (token) => {
                config.headers.Authorization = `Bearer ${token}`
                resolve(request(config))
              },
              reject
            })
          })
        }
        isRefreshing = true
        try {
          const newToken = await refreshToken(authStore.roleId)
          processQueue(newToken, null)
          config.headers.Authorization = `Bearer ${newToken}`
          return request(config)
        } catch (e) {
          processQueue(null, e)
          authStore.logout()
          router.push({ name: 'Login' })
          message.error('登录已过期，请重新登录')
          return Promise.reject(e)
        } finally {
          isRefreshing = false
        }
      } else {
        // 没有 token 或公开接口，直接跳登录
        authStore.logout()
        router.push({ name: 'Login' })
        message.error(errorData?.message || '请先登录')
        // F09: 显式 reject 避免落到下方通用 message.error 造成双重提示
        return Promise.reject(error)
      }
    } else if (status === 403) {
      const msg = errorData?.msg || '无权访问'
      message.error(msg)
      return Promise.reject(errorData)
    }
    message.error(error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
