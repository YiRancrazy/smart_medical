<template>
  <page-container title="就诊中">
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
              <a-button type="primary" size="small" @click="handleEditRecord(record)">编辑病历</a-button>
            </a-space>
          </template>
        </template>
        <template #emptyText>
          <a-empty description="暂无就诊中患者" />
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
import { getInProgressList } from '@/api/doctor/registration'
import type { WaitingPatientVO } from '@/api/doctor/registration'
import { formatDateTime } from '@/utils/format'
import { message } from 'ant-design-vue'

const router = useRouter()
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
    const res = await getInProgressList()
    tableData.value = res.data || []
  } catch {
    message.error('加载就诊中列表失败')
  } finally {
    loading.value = false
  }
}

function handleEditRecord(record: WaitingPatientVO) {
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
