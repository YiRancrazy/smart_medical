<template>
  <page-container :title="title">
    <glass-card>
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="患者姓名">
          <a-input v-model:value="searchForm.patientName" placeholder="请输入患者姓名" allow-clear />
        </a-form-item>
        <a-form-item label="创建日期">
          <a-range-picker v-model:value="dateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        class="data-table"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'visitDate'">
            {{ formatDateTime(record.visitDate) }}
          </template>
          <template v-if="column.key === 'createTime'">
            {{ formatDateTime(record.createTime) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
          </template>
        </template>
      </a-table>
    </glass-card>

    <a-drawer
      v-model:open="drawerVisible"
      title="病历详情"
      width="600px"
      :footer="null"
    >
      <a-spin :spinning="detailLoading">
        <a-descriptions v-if="detail" :column="1" bordered size="small">
          <a-descriptions-item label="病历ID">{{ detail.id }}</a-descriptions-item>
          <a-descriptions-item label="挂号记录ID">{{ detail.registrationId }}</a-descriptions-item>
          <a-descriptions-item label="患者姓名">{{ detail.patientName }}</a-descriptions-item>
          <a-descriptions-item label="患者手机号">{{ detail.patientPhone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="医生姓名">{{ detail.doctorName }}</a-descriptions-item>
          <a-descriptions-item label="科室名称">{{ detail.departmentName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="主诉">{{ detail.chiefComplaint || '-' }}</a-descriptions-item>
          <a-descriptions-item label="现病史">{{ detail.presentIllness || '-' }}</a-descriptions-item>
          <a-descriptions-item label="既往史">{{ detail.pastHistory || '-' }}</a-descriptions-item>
          <a-descriptions-item label="查体">{{ detail.physicalExam || '-' }}</a-descriptions-item>
          <a-descriptions-item label="诊断">{{ detail.diagnosis || '-' }}</a-descriptions-item>
          <a-descriptions-item label="治疗方案">{{ detail.treatmentPlan || '-' }}</a-descriptions-item>
          <a-descriptions-item label="状态">{{ detail.status === 1 ? '已提交' : '草稿' }}</a-descriptions-item>
        </a-descriptions>
      </a-spin>
    </a-drawer>
  </page-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import type { Dayjs } from 'dayjs'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import { formatDateTime } from '@/utils/format'
import type { Result, PageInfo } from '@/api/types'
import type { MedicalRecordPageItemVO, MedicalRecordDetailVO, MedicalRecordQueryParams } from '@/api/history/medicalRecord'
import { message } from 'ant-design-vue'

interface Props {
  title: string
  role: string
  pageApi: (role: string, params: MedicalRecordQueryParams) => Promise<Result<PageInfo<MedicalRecordPageItemVO>>>
  detailApi: (role: string, id: string | number) => Promise<Result<MedicalRecordDetailVO>>
}

const props = defineProps<Props>()

const searchForm = reactive({
  patientName: ''
})

const dateRange = ref<[Dayjs, Dayjs] | undefined>(undefined)

const tableData = ref<MedicalRecordPageItemVO[]>([])
const loading = ref(false)
const drawerVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<MedicalRecordDetailVO | null>(null)

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: '病历ID', dataIndex: 'id', key: 'id', width: 100 },
  { title: '挂号记录ID', dataIndex: 'registrationId', key: 'registrationId', width: 120 },
  { title: '患者姓名', dataIndex: 'patientName', key: 'patientName', width: 120 },
  { title: '医生姓名', dataIndex: 'doctorName', key: 'doctorName', width: 120 },
  { title: '科室名称', dataIndex: 'departmentName', key: 'departmentName', width: 140 },
  { title: '初步诊断', dataIndex: 'diagnosis', key: 'diagnosis', ellipsis: true },
  { title: '就诊日期', dataIndex: 'visitDate', key: 'visitDate', width: 160 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' }
]

onMounted(() => {
  loadData()
})

const queryParams = computed<MedicalRecordQueryParams>(() => {
  const params: MedicalRecordQueryParams = {
    patientName: searchForm.patientName || undefined,
    pageNum: pagination.value.current,
    pageSize: pagination.value.pageSize
  }
  if (dateRange.value && dateRange.value.length === 2) {
    params.startDate = dateRange.value[0].format('YYYY-MM-DD')
    params.endDate = dateRange.value[1].format('YYYY-MM-DD')
  }
  return params
})

async function loadData() {
  loading.value = true
  try {
    const res = await props.pageApi(props.role, queryParams.value)
    if (res.data) {
      tableData.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch {
    message.error('加载病历列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.current = 1
  loadData()
}

function handleReset() {
  searchForm.patientName = ''
  dateRange.value = undefined
  pagination.value.current = 1
  loadData()
}

function handleTableChange(pag: any) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

async function handleView(record: MedicalRecordPageItemVO) {
  detail.value = null
  drawerVisible.value = true
  detailLoading.value = true
  try {
    const res = await props.detailApi(props.role, record.id)
    detail.value = res.data || null
  } catch {
    message.error('加载病历详情失败')
  } finally {
    detailLoading.value = false
  }
}
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}

.data-table {
  margin-top: 16px;
}
</style>
