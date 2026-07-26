<template>
  <div class="page">
    <van-nav-bar title="我的处方" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" size="24px" />
    <empty-state v-else-if="!list.length" description="暂无处方记录" />
    <van-cell-group v-else inset>
      <van-cell
        v-for="item in list"
        :key="item.id"
        is-link
        @click="$router.push(`/prescription/${item.id}`)"
      >
        <template #title>
          <div class="cell-title">
            <span>处方单号 {{ item.id }}</span>
            <status-tag :status="item.status" type="prescription" />
          </div>
        </template>
        <template #label>
          <div class="cell-label">
            <div>共 {{ item.itemCount }} 种药品 · {{ formatDate(item.createTime) }}</div>
            <div class="amount">{{ formatMoney(item.totalAmount) }}</div>
          </div>
        </template>
      </van-cell>
    </van-cell-group>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { getPrescriptionList } from '@/api/prescription'
import type { PrescriptionListVO } from '@/api/prescription'
import { usePatientStore } from '@/stores/patient'
import EmptyState from '@/components/EmptyState.vue'
import StatusTag from '@/components/StatusTag.vue'
import { formatDate, formatMoney } from '@/utils/format'
import { showToast } from 'vant'

const loading = ref(false)
const list = ref<PrescriptionListVO[]>([])
const patientStore = usePatientStore()

// U08: 必须先 await patientStore.init() 再加载处方，否则 currentPatient 为 null 会取到全部处方
onMounted(async () => {
  await patientStore.init()
  await loadPrescriptions()
})

watch(() => patientStore.currentPatient, loadPrescriptions)

async function loadPrescriptions() {
  loading.value = true
  try {
    const cardId = patientStore.currentPatient?.patientCardId
    const res = await getPrescriptionList(cardId)
    list.value = res.data || []
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.cell-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 500;
}

.cell-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;

  .amount {
    color: #f56c6c;
    font-weight: 500;
  }
}
</style>
