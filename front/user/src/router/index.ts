import { createRouter, createWebHistory } from 'vue-router'
import { showDialog, showToast } from 'vant'
import routes from './routes'
import { getToken } from '@/utils/storage'

const router = createRouter({
  history: createWebHistory(),
  routes
})

const PROFILE_EXEMPT_PATHS = new Set([
  '/login',
  '/register',
  '/forgot-password',
  '/profile',
  '/profile/edit',
  '/change-password'
])
let profileDialogVisible = false

router.beforeEach(async (to, _from, next) => {
  const token = getToken()
  const requiresAuth = to.matched.some((r) => r.meta.requiresAuth)
  if (requiresAuth && !token) {
    next({ path: '/login', query: { redirect: to.fullPath }, replace: true })
  } else if (to.path === '/login' && token) {
    next({ path: '/', replace: true })
  } else {
    if (!requiresAuth || PROFILE_EXEMPT_PATHS.has(to.path)) {
      next()
      return
    }

    try {
      const { useUserStore } = await import('@/stores/user')
      const completed = await useUserStore().ensureProfileCompleted()
      if (completed) {
        next()
        return
      }

      next({ path: '/profile/edit', replace: true })
      if (!profileDialogVisible) {
        profileDialogVisible = true
        showDialog({
          title: '请完善个人信息',
          message: '补充姓名、身份证号和性别后，才能继续使用其他功能。',
          confirmButtonText: '去补充',
          showCancelButton: false,
          closeOnClickOverlay: false,
          closeOnPopstate: false
        }).finally(() => {
          profileDialogVisible = false
        })
      }
    } catch {
      showToast('个人信息状态加载失败，请稍后重试')
      next(false)
    }
  }
})

export default router
