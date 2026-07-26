import { createRouter, createWebHistory } from 'vue-router'
import routes from './routes'
import { getToken } from '@/utils/storage'

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = getToken()
  const requiresAuth = to.matched.some((r) => r.meta.requiresAuth)
  if (requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath }, replace: true })
  } else if (to.path === '/login' && token) {
    next({ path: '/', replace: true })
  } else {
    next()
  }
})

export default router
