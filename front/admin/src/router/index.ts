import { createRouter, createWebHistory } from 'vue-router'
import { publicRoutes } from './routes'
import { getRoleRoutes } from './modules'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: publicRoutes
})

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.public) return next()

  if (!authStore.token) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  if (!authStore.userInfo) {
    try {
      await authStore.fetchCurrentUser()
    } catch {
      authStore.logout()
      return next({ name: 'Login' })
    }
  }

  // F36: roleId 为 null（token 无 role claim）时登出跳登录，避免 getRoleRoutes(null) 返回空导致空白布局
  if (!authStore.roleId) {
    authStore.logout()
    return next({ name: 'Login' })
  }

  const requiredRoles = to.meta.roles as number[] | undefined
  if (requiredRoles && !requiredRoles.includes(authStore.roleId)) {
    return next({ name: 'Forbidden' })
  }

  if (!authStore.routesLoaded) {
    const routes = getRoleRoutes(authStore.roleId)
    routes.forEach((r) => router.addRoute(r))
    authStore.routesLoaded = true
    return next({ ...to, replace: true })
  }

  next()
})

export default router
