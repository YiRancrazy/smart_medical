<template>
  <div class="app-layout">
    <div class="app-content">
      <router-view />
    </div>
    <van-tabbar v-model="active" safe-area-inset-bottom class="app-tabbar">
      <van-tabbar-item icon="home-o" @click="switchTab('/')">首页</van-tabbar-item>
      <van-tabbar-item icon="records-o" @click="switchTab('/registration')">我的挂号</van-tabbar-item>
      <van-tabbar-item icon="friends-o" @click="switchTab('/patient')">就诊人</van-tabbar-item>
      <van-tabbar-item icon="user-o" @click="switchTab('/profile')">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const active = ref(0)
const map: Record<string, number> = { Home: 0, RegistrationList: 1, PatientList: 2, Profile: 3 }

watch(
  () => route.name,
  (name) => {
    active.value = map[name as string] ?? 0
  },
  { immediate: true }
)

function switchTab(path: string) {
  router.push(path)
}
</script>

<style scoped lang="scss">
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.app-content {
  flex: 1;
  padding-bottom: calc(50px + env(safe-area-inset-bottom));
}

.app-tabbar {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
}
</style>
