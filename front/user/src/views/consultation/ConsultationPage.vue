<template>
  <div class="consultation-page">
    <van-nav-bar title="在线咨询" left-arrow @click-left="router.back()" />

    <div class="content">
      <van-empty description="在线咨询功能已开通，请选择医生进行咨询" />
      <div class="actions">
        <van-button type="primary" block @click="router.push('/department')">
          选择医生
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.consultation-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 46px);
}

.actions {
  width: 100%;
  max-width: 400px;
  padding: 16px;
}
</style>