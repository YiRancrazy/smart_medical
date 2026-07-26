<template>
  <div class="page not-found">
    <van-empty description="页面不存在">
      <template #default>
        <van-button round type="primary" class="back-btn" @click="router.replace('/')">
          返回首页
        </van-button>
      </template>
    </van-empty>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped lang="scss">
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: #f5f5f5;
}

.back-btn {
  margin-top: 16px;
  width: 160px;
}
</style>
