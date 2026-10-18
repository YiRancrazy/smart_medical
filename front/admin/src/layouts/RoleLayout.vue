<template>
  <a-layout class="admin-layout">
    <a-layout-sider
      v-model:collapsed="appStore.sidebarCollapsed"
      :trigger="null"
      collapsible
      theme="light"
      class="layout-sider"
      width="220"
    >
      <div class="logo">Smart Medical</div>
      <a-menu
        mode="inline"
        :selectedKeys="selectedKeys"
        :items="menuItems"
        @click="handleMenuClick"
      />
    </a-layout-sider>
    <a-layout class="layout-main">
      <a-layout-header class="layout-header">
        <menu-unfold-outlined
          v-if="appStore.sidebarCollapsed"
          class="trigger"
          @click="appStore.toggleSidebar"
        />
        <menu-fold-outlined v-else class="trigger" @click="appStore.toggleSidebar" />
        <span class="header-title">{{ title }}</span>
        <a-dropdown>
          <span class="user-action">
            <user-outlined />
            {{ authStore.userInfo?.phone || authStore.roleName }}
          </span>
          <template #overlay>
            <a-menu>
              <!-- F35: 包 async handler 确保后端登出完成后再跳转 -->
              <a-menu-item @click="handleLogout">退出登录</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-layout-header>
      <a-layout-content class="layout-content">
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { MenuUnfoldOutlined, MenuFoldOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useSideMenu } from '@/composables/usePermission'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

defineProps<{
  title: string
}>()

const appStore = useAppStore()
const authStore = useAuthStore()
const router = useRouter()
const { menuItems, selectedKeys } = useSideMenu()

function handleMenuClick({ key }: { key: string }) {
  const item = menuItems.value.find((i) => i.key === key)
  if (item) router.push(item.path)
}

async function handleLogout() {
  await authStore.logoutWithApi()
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.trigger {
  font-size: @font-size-lg;
  cursor: pointer;
  color: @text-color-secondary;
}

.user-action {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: @spacing-sm;
}
</style>
