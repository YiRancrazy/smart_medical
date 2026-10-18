<template>
  <page-container title="候诊列表">
    <glass-card>
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        row-key="registrationId"
        class="data-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <RegistrationStatus :status="record.status" />
          </template>
          <template v-if="column.key === 'checkInTime'">
            {{ formatTime(record.checkInTime) }}
          </template>
          <template v-if="column.key === 'registrationTime'">
            {{ formatTime(record.registrationTime) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="primary" size="small" @click="handleCall(record)">叫号</a-button>
            </a-space>
          </template>
        </template>
        <template #emptyText>
          <a-empty description="暂无候诊患者" />
        </template>
      </a-table>
    </glass-card>
  </page-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import RegistrationStatus from '@/components/business/RegistrationStatus.vue'
import { getWaitingList, callPatient } from '@/api/doctor/registration'
import type { WaitingPatientVO } from '@/api/doctor/registration'
import { formatDateTime } from '@/utils/format'
import { message } from 'ant-design-vue'

const tableData = ref<WaitingPatientVO[]>([])
const loading = ref(false)

const columns = [
  { title: '挂号记录ID', dataIndex: 'registrationId', key: 'registrationId', width: 160 },
  { title: '患者姓名', dataIndex: 'patientName', key: 'patientName', width: 120 },
  { title: '患者手机号', dataIndex: 'patientPhone', key: 'patientPhone', width: 140 },
  { title: '挂号状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '签到时间', dataIndex: 'checkInTime', key: 'checkInTime', width: 180 },
  { title: '挂号时间', dataIndex: 'registrationTime', key: 'registrationTime', width: 180 },
  { title: '操作', key: 'action', width: 120, fixed: 'right' }
]

function formatTime(val: string) {
  return formatDateTime(val)
}

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getWaitingList()
    tableData.value = res.data || []
  } catch {
    message.error('加载候诊列表失败')
  } finally {
    loading.value = false
  }
}

async function handleCall(record: WaitingPatientVO) {
  try {
    await callPatient(record.registrationId)
    message.success('叫号成功')
    loadData()
  } catch {
    message.error('叫号失败')
  }
}
</script>

<style scoped>
.data-table {
  margin-top: 16px;
}
</style>
