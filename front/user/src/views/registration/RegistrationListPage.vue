<template>
  <div class="page">
    <van-nav-bar title="我的挂号" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" size="24px" />
    <template v-else>
      <van-tabs v-model:active="activeTab" sticky>
        <van-tab title="待就诊">
          <reg-list :list="pendingList" show-actions @checkin="goCheckin" @cancel="onCancel" @pay="goPay" />
        </van-tab>
        <van-tab title="已就诊">
          <reg-list :list="doneList" show-actions />
        </van-tab>
        <van-tab title="已取消">
          <reg-list :list="cancelledList" show-actions />
        </van-tab>
      </van-tabs>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getRegistrationList, cancelRegistration } from '@/api/registration'
import type { AppointmentResponseSimple } from '@/api/registration'
import { useRegistrationStore } from '@/stores/registration'
import { usePatientStore } from '@/stores/patient'
import { showToast, showConfirmDialog } from 'vant'
import RegList from './RegList.vue'

const router = useRouter()
const regStore = useRegistrationStore()
const patientStore = usePatientStore()
const loading = ref(false)
const activeTab = ref(0)
const list = ref<AppointmentResponseSimple[]>([])

// 后端 RegistrationStatusEnum：0待支付 1待就诊 2支付失败 3取消 4完成 5已报到 6就诊中 7待支付(处方补缴)
// 按业务状态分组：待就诊=待处理/待就诊(0,1,2,7，有操作按钮)；已就诊=已报到/就诊中/完成(4,5,6)；已取消=3
const pendingList = computed(() => list.value.filter(r => [0, 1, 2, 7].includes(r.status)))
const doneList = computed(() => list.value.filter(r => [4, 5, 6].includes(r.status)))
const cancelledList = computed(() => list.value.filter(r => r.status === 3))

onMounted(async () => {
  await patientStore.init()
  await loadRegistrations()
})

watch(() => patientStore.currentPatient, loadRegistrations)

async function loadRegistrations() {
  loading.value = true
  try {
    const cardId = patientStore.currentPatient?.patientCardId
    const res = await getRegistrationList(cardId)
    // U27: 后端返回 PageResult，取 .list
    list.value = res.data?.list || []
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

function goCheckin(id: string) {
  router.push(`/checkin/${id}`)
}

function goPay(reg: AppointmentResponseSimple) {
  if (!reg.orderId) {
    showToast('缺少订单信息')
    return
  }
  regStore.setOrderId(reg.orderId)
  // U01: 后端 registrationPrice 已返回分，realAmount 也期望分，直接透传
  router.push(`/registration/payment?orderId=${reg.orderId}&amount=${reg.registrationPrice || 0}`)
}

async function onCancel(id: string) {
  try {
    await showConfirmDialog({ title: '确认取消', message: '取消后需重新挂号，确认取消吗？' })
  } catch {
    return // 用户点取消
  }
  try {
    await cancelRegistration(id)
    showToast('已取消')
    await loadRegistrations()
  } catch {
    showToast('取消失败，请重试')
  }
}
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f5f5f5;
}
</style>
