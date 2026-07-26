<template>
  <div class="error-page">
    <!-- F32: 补返回入口，避免用户卡死在 403 页 -->
    <a-result status="403" title="403" sub-title="无权访问该页面">
      <template #extra>
        <a-button type="primary" @click="goLogin">返回登录</a-button>
      </template>
    </a-result>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

function goLogin() {
  authStore.logout()
}
</script>

<style scoped lang="less">
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
