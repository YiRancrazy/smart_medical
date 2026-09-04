import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { setToken, setUid, setUserInfo, clearAuth, getToken, getUid, getUserInfo } from '@/utils/storage'
import { usePatientStore } from '@/stores/patient'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const uid = ref<string | null>(getUid())
  const userInfo = ref<any>(getUserInfo())

  const isLoggedIn = computed(() => !!token.value)

  /**
   * 写入登录态（登录/注册自动登录共用）
   */
  function applyLoginData(data: any) {
    if (!data || !data.token) {
      throw new Error('登录失败：未获取到token')
    }
    token.value = data.token
    uid.value = String(data.uid)
    userInfo.value = { phone: data.phone, name: data.userName }
    setToken(data.token)
    setUid(String(data.uid))
    setUserInfo(userInfo.value)
  }

  async function login(phone: string, password: string) {
    const res = await authApi.login({ phone, password })
    applyLoginData(res.data)

    // U05: 优先回跳原目标页，无 redirect 才落首页
    const redirect = typeof router.currentRoute.value.query.redirect === 'string'
      ? router.currentRoute.value.query.redirect
      : ''
    await router.replace(redirect || '/')
  }

  async function register(phone: string, code: string) {
    const res = await authApi.register({ phone, code })
    applyLoginData(res.data)
    await router.replace('/')
  }

  function logout() {
    token.value = null
    uid.value = null
    userInfo.value = null
    clearAuth()
    // U19: 重置 patientStore，避免换账号后残留上一用户的就诊人数据
    usePatientStore().reset()
    router.push('/login')
  }

  /**
   * 用户主动登出：先调后端清 Redis token，再清前端
   * ponytail: 拦截器 401 时只调 logout()（纯前端），避免触发后端 401 死循环
   */
  async function logoutWithApi() {
    try {
      await authApi.logout()
    } catch (e) {
      console.warn('[logout] 后端登出失败，仍清理前端', e)
    }
    logout()
  }

  return { token, uid, userInfo, isLoggedIn, login, register, logout, logoutWithApi }
})
