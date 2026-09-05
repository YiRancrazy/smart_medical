import { createRouter, createWebHistory } from 'vue-router'
import { publicRoutes } from './routes'
import { getRoleRoutes } from './modules'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: publicRoutes
})

// L5: 已动态注册的顶层路由名，用于登出/换角色时 removeRoute 清理
const dynamicRouteNames: (string | symbol)[] = []

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
    // L5: 重登/换角色前先移除上次动态注册的顶层路由，避免重复 addRoute
    dynamicRouteNames.forEach((name) => router.removeRoute(name))
    dynamicRouteNames.length = 0
    const routes = getRoleRoutes(authStore.roleId)
    routes.forEach((r) => {
      router.addRoute(r)
      if (r.name) dynamicRouteNames.push(r.name)
    })
    authStore.routesLoaded = true
    return next({ ...to, replace: true })
  }

  next()
})

export default router
