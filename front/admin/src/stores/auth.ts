import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import router from '@/router'
import { useRoute } from 'vue-router'
import { loginByRole, getCurrentByRole, logoutByRole } from '@/api/admin/auth'

export const useAuthStore = defineStore(
  'auth',
  () => {
    const route = useRoute()
    const token = ref<string | null>(localStorage.getItem('sm_admin_token'))
    const roleId = ref<number | null>(parseRoleId(token.value))
    const userInfo = ref<any>(null)
    const routesLoaded = ref(false)

    // BUG-F04: token 存在但解析失败，说明 token 已损坏，清理为未登录状态避免白屏/路由异常
    if (token.value && roleId.value === null) {
      localStorage.removeItem('sm_admin_token')
      token.value = null
    }

    function parseRoleId(t: string | null): number | null {
      if (!t) return null
      try {
        // base64url → base64：替换 -_ 为 +/，补齐 padding
        const parts = t.split('.')
        if (parts.length < 2 || !parts[1]) return null
        let p = parts[1].replace(/-/g, '+').replace(/_/g, '/')
        while (p.length % 4) p += '='
        const payload = JSON.parse(atob(p))
        return payload.role ?? null
      } catch {
        return null
      }
    }

    const isLoggedIn = computed(() => !!token.value)
    const rolePath = computed(() => {
      if (roleId.value === 1) return '/admin'
      if (roleId.value === 2) return '/doctor'
      if (roleId.value === 6) return '/pharmacy'
      return '/login'
    })
    const roleName = computed(() => {
      if (roleId.value === 1) return '管理员'
      if (roleId.value === 2) return '医生'
      if (roleId.value === 6) return '药师'
      return ''
    })

    function applyToken(t: string) {
      token.value = t
      localStorage.setItem('sm_admin_token', t)
      roleId.value = parseRoleId(t)
    }

    async function login(phone: string, password: string, remember: boolean, role: number) {
      const res = await loginByRole(role, { phone, password, remember })
      const t = res?.data
      if (!t) throw new Error('登录失败，未获取到 token')
      applyToken(t)
      await fetchCurrentUser(role)
      if (!userInfo.value) {
        throw new Error('获取用户信息失败')
      }
      // F11: 优先回跳原目标页，无 redirect 才落 rolePath
      const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
      await router.replace(redirect || rolePath.value)
    }

    async function fetchCurrentUser(role?: number) {
      const r = role ?? roleId.value
      if (!r) return
      try {
        const res = await getCurrentByRole(r)
        userInfo.value = res.data
      } catch (error) {
        console.error('[fetchCurrentUser] 失败', error)
        throw error
      }
    }

    function logout() {
      token.value = null
      roleId.value = null
      userInfo.value = null
      routesLoaded.value = false
      localStorage.removeItem('sm_admin_token')
      router.push({ name: 'Login' })
    }

    /**
     * 用户主动登出：先调后端清 Redis token，再清前端
     * F08: 按角色调对应登出端点，原硬编码 admin 接口导致医生/药师后端 token 未清
     * ponytail: 拦截器 401 时只调 logout()（纯前端），避免触发后端 401 死循环
     */
    async function logoutWithApi() {
      try {
        const role = roleId.value
        if (role) {
          await logoutByRole(role)
        }
      } catch (e) {
        console.warn('[logout] 后端登出失败，仍清理前端', e)
      }
      logout()
    }

    return {
      token,
      roleId,
      userInfo,
      routesLoaded,
      isLoggedIn,
      rolePath,
      roleName,
      applyToken,
      login,
      fetchCurrentUser,
      logout,
      logoutWithApi
    }
  },
  { persist: { paths: ['token'] } }
)
