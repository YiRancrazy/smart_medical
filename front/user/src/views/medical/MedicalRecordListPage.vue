<template>
  <div class="page">
    <van-nav-bar title="我的病历" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" size="24px" />
    <empty-state v-else-if="!list.length" description="暂无病历记录" />
    <van-cell-group v-else inset>
      <van-cell
        v-for="item in list"
        :key="item.id"
        is-link
        @click="$router.push(`/medical-record/${item.id}`)"
      >
        <template #title>
          <div class="cell-title">
            <span>{{ item.departmentName }} - {{ item.doctorName }}</span>
          </div>
        </template>
        <template #label>
          <div class="cell-label">
            <div>{{ item.visitDate }}</div>
            <div class="diagnosis">{{ item.diagnosis }}</div>
          </div>
        </template>
      </van-cell>
    </van-cell-group>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { getMedicalRecordList } from '@/api/medicalRecord'
import type { MedicalRecordListVO } from '@/api/medicalRecord'
import { usePatientStore } from '@/stores/patient'
import EmptyState from '@/components/EmptyState.vue'
import { showToast } from 'vant'

const loading = ref(false)
const list = ref<MedicalRecordListVO[]>([])
const patientStore = usePatientStore()

onMounted(async () => {
  await patientStore.init()
  await loadRecords()
})

watch(() => patientStore.currentPatient, loadRecords)

async function loadRecords() {
  loading.value = true
  try {
    const cardId = patientStore.currentPatient?.patientCardId
    const res = await getMedicalRecordList(cardId)
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
  font-weight: 500;
}

.cell-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;

  .diagnosis {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
