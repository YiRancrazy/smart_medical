import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { getSelfPatientCardStatus } from '@/api/patient'
import { getUserProfile, type UserProfile } from '@/api/user'
import { setToken, setUid, setUserInfo, clearAuth, getToken, getUid, getUserInfo } from '@/utils/storage'
import { usePatientStore } from '@/stores/patient'
import { useRegistrationStore } from '@/stores/registration'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref<string | null>(getToken())
  const uid = ref<string | null>(getUid())
  const userInfo = ref<any>(getUserInfo())
  const profileCompleted = ref<boolean | null>(userInfo.value?.profileCompleted ?? null)
  const ownPatientCardCompleted = ref<boolean | null>(null)
  let profileRequest: Promise<boolean> | null = null
  let ownPatientCardRequest: Promise<boolean> | null = null

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
    profileCompleted.value = data.profileCompleted === true
    userInfo.value = {
      phone: data.phone,
      name: data.userName,
      profileCompleted: profileCompleted.value
    }
    setToken(data.token)
    setUid(String(data.uid))
    setUserInfo(userInfo.value)
  }

  /**
   * 确保已读取个人信息完善状态，接口异常由调用方处理
   */
  async function ensureProfileCompleted(force = false): Promise<boolean> {
    if (!token.value) return false
    if (!force && profileCompleted.value !== null) return profileCompleted.value
    if (profileRequest) return profileRequest

    profileRequest = getUserProfile()
      .then((res) => {
        applyProfile(res.data)
        return profileCompleted.value === true
      })
      .finally(() => {
        profileRequest = null
      })
    return profileRequest
  }

  /**
   * 确保已读取本人就诊卡完善状态，接口异常由调用方处理
   */
  async function ensureOwnPatientCardCompleted(force = false): Promise<boolean> {
    if (!token.value) return false
    if (!force && ownPatientCardCompleted.value !== null) return ownPatientCardCompleted.value
    if (ownPatientCardRequest) return ownPatientCardRequest

    ownPatientCardRequest = getSelfPatientCardStatus()
      .then((res) => {
        ownPatientCardCompleted.value = res.data?.completed === true
        return ownPatientCardCompleted.value
      })
      .finally(() => {
        ownPatientCardRequest = null
      })
    return ownPatientCardRequest
  }

  /**
   * 同步个人信息页保存结果
   */
  function applyProfile(profile: UserProfile) {
    profileCompleted.value = profile?.profileCompleted === true
    userInfo.value = {
      ...(userInfo.value || {}),
      name: profile?.username || userInfo.value?.name,
      profileCompleted: profileCompleted.value
    }
    setUserInfo(userInfo.value)
  }

  /**
   * 登录后回跳：优先回跳原目标页，无 redirect 才落首页
   */
  async function goAfterLogin() {
    const redirect = typeof router.currentRoute.value.query.redirect === 'string'
      ? router.currentRoute.value.query.redirect
      : ''
    await router.replace(redirect || '/')
  }

  async function login(phone: string, password: string) {
    const res = await authApi.login({ phone, password })
    applyLoginData(res.data)
    await goAfterLogin()
  }

  async function loginByCode(phone: string, code: string) {
    const res = await authApi.loginByCode({ phone, code })
    applyLoginData(res.data)
    await goAfterLogin()
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
    profileCompleted.value = null
    ownPatientCardCompleted.value = null
    clearAuth()
    // U19: 重置 patientStore，避免换账号后残留上一用户的就诊人数据
    usePatientStore().reset()
    // M7: 清理挂号流程状态（sessionStorage 中的已选排班/订单），换账号不残留上次挂号数据
    useRegistrationStore().resetFlow()
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

  return {
    token,
    uid,
    userInfo,
    profileCompleted,
    ownPatientCardCompleted,
    isLoggedIn,
    login,
    loginByCode,
    register,
    logout,
    logoutWithApi,
    ensureProfileCompleted,
    ensureOwnPatientCardCompleted,
    applyProfile
  }
})
