<template>
  <page-container title="今日排班">
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
          <template v-if="column.key === 'startTime'">
            {{ formatTime(record.startTime) }}
          </template>
          <template v-if="column.key === 'endTime'">
            {{ formatTime(record.endTime) }}
          </template>
          <template v-if="column.key === 'registrationTime'">
            {{ formatTime(record.registrationTime) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleCall(record)">叫号</a-button>
            <a-button type="link" size="small" @click="handleEditRecord(record)">病历</a-button>
          </template>
        </template>
        <template #emptyText>
          <a-empty description="今日暂无排班记录" />
        </template>
      </a-table>
    </glass-card>
  </page-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import RegistrationStatus from '@/components/business/RegistrationStatus.vue'
import { getTodaySchedule } from '@/api/doctor/schedule'
import { callPatient } from '@/api/doctor/registration'
import type { DoctorScheduleVO } from '@/api/doctor/schedule'
import { formatDateTime } from '@/utils/format'
import { message } from 'ant-design-vue'

const router = useRouter()
const tableData = ref<DoctorScheduleVO[]>([])
const loading = ref(false)

const columns = [
  { title: '挂号记录ID', dataIndex: 'registrationId', key: 'registrationId', width: 160 },
  { title: '班次名称', dataIndex: 'shiftName', key: 'shiftName', width: 120 },
  { title: '挂号状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '开始时间', dataIndex: 'startTime', key: 'startTime', width: 180 },
  { title: '结束时间', dataIndex: 'endTime', key: 'endTime', width: 180 },
  { title: '患者姓名', dataIndex: 'patientName', key: 'patientName', width: 120 },
  { title: '患者手机号', dataIndex: 'patientPhone', key: 'patientPhone', width: 140 },
  { title: '挂号时间', dataIndex: 'registrationTime', key: 'registrationTime', width: 180 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' }
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
    const res = await getTodaySchedule()
    tableData.value = res.data || []
  } catch {
    message.error('加载今日排班失败')
  } finally {
    loading.value = false
  }
}

async function handleCall(record: DoctorScheduleVO) {
  try {
    await callPatient(record.registrationId)
    message.success('叫号成功')
    router.push({ name: 'WaitingList' })
  } catch {
    message.error('叫号失败')
  }
}

function handleEditRecord(record: DoctorScheduleVO) {
  router.push({
    name: 'MedicalRecordEdit',
    query: {
      registrationId: record.registrationId,
      patientName: record.patientName,
      patientPhone: record.patientPhone
    }
  })
}
</script>

<style scoped>
.data-table {
  margin-top: 16px;
}
</style>
