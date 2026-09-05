<template>
  <div class="page">
    <van-nav-bar title="报到" left-arrow @click-left="$router.back()" />
    <glass-card class="card">
      <van-loading v-if="loading" size="24px" />
      <template v-else-if="!reg">
        <empty-state :description="loadError || '未找到挂号信息'" />
        <van-button round block type="primary" class="btn" @click="router.back()">返回</van-button>
      </template>
      <template v-else>
        <div class="section-title">挂号信息</div>
        <van-cell title="医生" :value="reg?.doctorName" />
        <van-cell title="科室" :value="reg?.departmentName" />
        <van-cell title="就诊时间" :value="`${reg?.scheduleDate} ${reg?.scheduleTime}`" />
        <van-button round block type="primary" class="btn" :loading="checking" @click="onCheckIn">报到</van-button>
      </template>
    </glass-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { checkIn, getRegistrationDetail } from '@/api/registration'
import type { AppointmentResponseSimple } from '@/api/registration'
import GlassCard from '@/components/GlassCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const checking = ref(false)
const loadError = ref('')
const reg = ref<AppointmentResponseSimple | null>(null)

// U14: route.params.id 缺失时 String(undefined)="undefined" 是 truthy，需先判原始值
const rawId = route.params.id
const id = rawId ? String(rawId) : ''

onMounted(async () => {
  if (!id) {
    showToast('缺少挂号记录参数')
    router.back()
    loading.value = false
    return
  }
  try {
    const res = await getRegistrationDetail(id)
    reg.value = res.data || null
    if (!res.data) loadError.value = '未找到挂号信息'
  } catch (e) {
    loadError.value = (e as Error)?.message || '挂号信息加载失败'
  }
  loading.value = false
})

async function onCheckIn() {
  if (!id) return
  checking.value = true
  try {
    await checkIn(id)
    showToast({ message: '报到成功，请等待叫号', type: 'success' })
    router.replace('/registration')
  } catch {
    showToast('报到失败')
  } finally {
    checking.value = false
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $color-bg-page;
  padding-bottom: 24px;
}

.card {
  margin: 16px;
}

.section-title {
  font-size: $font-size-h3;
  font-weight: $font-weight-semibold;
  margin-bottom: 12px;
  color: $color-text-primary;
}

.btn {
  margin-top: 16px;
}
</style>
