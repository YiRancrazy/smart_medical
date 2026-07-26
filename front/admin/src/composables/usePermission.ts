import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

export function useSideMenu() {
  const authStore = useAuthStore()
  const router = useRouter()
  const route = useRoute()

  const menuItems = computed(() => {
    // 必须从 getRoutes() 读取，因为 addRoute() 动态注册的路由不在 options.routes 中
    const allRoutes = router.getRoutes()
    const rootRoute = allRoutes.find((r) =>
      (r.meta?.roles as number[] | undefined)?.includes(authStore.roleId!)
    )
    return (
      rootRoute?.children?.map((child) => {
        const iconComp = child.meta?.icon
        return {
          key: child.name as string,
          label: (child.meta?.title as string) || '',
          // antdv Menu items 要求 icon 为 VNode/render 函数
          icon: iconComp ? () => h(iconComp as any) : undefined,
          path: child.path.startsWith('/') ? child.path : `${rootRoute.path}/${child.path}`
        }
      }) ?? []
    )
  })

  const selectedKeys = computed(() => [route.name as string])

  return { menuItems, selectedKeys }
}
