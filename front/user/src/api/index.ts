import axios from 'axios'
import { showToast } from 'vant'
import router from '@/router'
import { getToken, setToken, clearAuth } from '@/utils/storage'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
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

async function refreshToken(): Promise<string> {
  const maxRetry = 3
  let lastErr: any
  for (let i = 0; i < maxRetry; i++) {
    try {
      // U20: dev 环境走 vite proxy 同源请求，避免跨域 POST 无法携带 HttpOnly Cookie
      const base = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
      const url = base ? `${base}/api/user/v1/auth/refresh` : '/api/user/v1/auth/refresh'
      const res = await axios.post(
        url,
        {},
        { withCredentials: true }
      )
      const body = res.data as any
      if (body?.code !== 200) {
        throw new Error(body?.message || '刷新失败')
      }
      // 优先从响应头读取 token（与登录一致），响应体 data 字段兜底（CORS 未暴露 Authorization 时仍可用）
      const headerToken = res.headers.authorization?.replace('Bearer ', '')
      const bodyToken = body?.data
      const newToken = headerToken || bodyToken
      if (newToken) {
        setToken(newToken)
        // U09: 同步到 userStore，避免 store.isLoggedIn 与实际状态不一致
        useUserStore().token = newToken
        return newToken
      }
      throw new Error('刷新接口未返回 token')
    } catch (e: any) {
      lastErr = e
      // refresh token 过期/无效时后端返回 401，无需重试
      if (e?.response?.status === 401) break
      if (i < maxRetry - 1) await new Promise(r => setTimeout(r, 500))
    }
  }
  throw lastErr
}

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const res = response.data as any
    if (res && typeof res.code === 'number') {
      if (res.code === 200) return res
      showToast(res.message || '操作失败')
      return Promise.reject(new Error(res.message))
    }
    return res
  },
  async (error) => {
    const { status, data, config } = error.response || {}
    if (status === 401) {
      const token = getToken()
      // 只有在有 token 且不是公开接口时才尝试刷新
      const isPublicApi = config.url?.includes('/auth/login') ||
                          config.url?.includes('/auth/register') ||
                          config.url?.includes('/auth/refresh')

      if (token && !isPublicApi) {
        if (isRefreshing) {
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve: (token) => {
              config.headers.Authorization = `Bearer ${token}`
              resolve(request(config))
            }, reject })
          })
        }
        isRefreshing = true
        try {
          const newToken = await refreshToken()
          processQueue(newToken, null)
          config.headers.Authorization = `Bearer ${newToken}`
          return request(config)
        } catch (e) {
          processQueue(null, e)
          // U09: 清 store 状态，避免 isLoggedIn 与实际不一致
          useUserStore().logout()
          showToast('认证失败，请重新登录')
          return Promise.reject(e)
        } finally {
          isRefreshing = false
        }
      } else {
        // U16: 公开接口 401 应透传后端错误，不补默认"请先登录"误导用户
        const isPublic = config.url?.includes('/auth/login') || config.url?.includes('/auth/register')
        if (isPublic) {
          showToast(data?.message || '请求失败')
        } else {
          useUserStore().logout()
          showToast(data?.message || '请先登录')
        }
      }
    } else if (status === 403) {
      clearAuth()
      router.push('/login')
      showToast(data?.msg || '无权访问')
    } else if (!status) {
      showToast('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default request
