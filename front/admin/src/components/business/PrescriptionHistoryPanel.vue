<template>
  <page-container :title="title">
    <glass-card>
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="患者姓名">
          <a-input v-model:value="searchForm.patientName" placeholder="请输入患者姓名" allow-clear />
        </a-form-item>
        <a-form-item label="处方状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 160px"
          >
            <a-select-option :value="0">待支付</a-select-option>
            <a-select-option :value="1">已支付</a-select-option>
            <a-select-option :value="2">已发药</a-select-option>
            <a-select-option :value="3">已取消</a-select-option>
          </a-select>
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
          <template v-if="column.key === 'totalAmount'">
            {{ formatMoney(record.totalAmount / 100) }}
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="getStatusColor(record.status)">{{ getStatusText(record.status) }}</a-tag>
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
      title="处方详情"
      width="700px"
      :footer="null"
    >
      <a-spin :spinning="detailLoading">
        <template v-if="detail">
          <a-descriptions :column="1" bordered size="small" class="detail-desc">
            <a-descriptions-item label="处方ID">{{ detail.id }}</a-descriptions-item>
            <a-descriptions-item label="病历ID">{{ detail.medicalRecordId }}</a-descriptions-item>
            <a-descriptions-item label="患者姓名">{{ detail.patientName }}</a-descriptions-item>
            <a-descriptions-item label="患者手机号">{{ detail.patientPhone || '-' }}</a-descriptions-item>
            <a-descriptions-item label="医生姓名">{{ detail.doctorName }}</a-descriptions-item>
            <a-descriptions-item label="处方状态">
              <a-tag :color="getStatusColor(detail.status)">{{ getStatusText(detail.status) }}</a-tag>
            </a-descriptions-item>
            <a-descriptions-item label="处方金额">{{ formatMoney(detail.totalAmount / 100) }}</a-descriptions-item>
            <a-descriptions-item label="关联订单ID">{{ detail.orderId || '-' }}</a-descriptions-item>
            <a-descriptions-item label="创建时间">{{ formatDateTime(detail.createTime) }}</a-descriptions-item>
          </a-descriptions>

          <h4 class="section-title">药品明细</h4>
          <a-table
            :columns="itemColumns"
            :data-source="detail.items"
            row-key="drugId"
            size="small"
            :pagination="false"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'unitPrice'">
                {{ formatMoney(record.unitPrice / 100) }}
              </template>
            </template>
          </a-table>
        </template>
      </a-spin>
    </a-drawer>
  </page-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import type { Dayjs } from 'dayjs'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import { formatDateTime, formatMoney } from '@/utils/format'
import type { Result, PageInfo } from '@/api/types'
import type { PrescriptionPageItemVO, PrescriptionDetailVO, PrescriptionQueryParams } from '@/api/history/prescription'
import { message } from 'ant-design-vue'

interface Props {
  title: string
  role: string
  pageApi: (role: string, params: PrescriptionQueryParams) => Promise<Result<PageInfo<PrescriptionPageItemVO>>>
  detailApi: (role: string, id: string | number) => Promise<Result<PrescriptionDetailVO>>
}

const props = defineProps<Props>()

const searchForm = reactive({
  patientName: '',
  status: undefined as number | undefined
})

const dateRange = ref<[Dayjs, Dayjs] | undefined>(undefined)

const tableData = ref<PrescriptionPageItemVO[]>([])
const loading = ref(false)
const drawerVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<PrescriptionDetailVO | null>(null)

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: '处方ID', dataIndex: 'id', key: 'id', width: 100 },
  { title: '病历ID', dataIndex: 'medicalRecordId', key: 'medicalRecordId', width: 120 },
  { title: '患者姓名', dataIndex: 'patientName', key: 'patientName', width: 120 },
  { title: '医生姓名', dataIndex: 'doctorName', key: 'doctorName', width: 120 },
  { title: '处方金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '处方状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '药品项数量', dataIndex: 'itemCount', key: 'itemCount', width: 100 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 160 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' }
]

const itemColumns = [
  { title: '药品ID', dataIndex: 'drugId', key: 'drugId', width: 100 },
  { title: '药品名称', dataIndex: 'commonName', key: 'commonName' },
  { title: '规格', dataIndex: 'specification', key: 'specification', width: 120 },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 80 },
  { title: '单价', dataIndex: 'unitPrice', key: 'unitPrice', width: 100 },
  { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 80 },
  { title: '用法用量', dataIndex: 'usageMethod', key: 'usageMethod', ellipsis: true }
]

onMounted(() => {
  loadData()
})

const queryParams = computed<PrescriptionQueryParams>(() => {
  const params: PrescriptionQueryParams = {
    patientName: searchForm.patientName || undefined,
    status: searchForm.status,
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
    message.error('加载处方列表失败')
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
  searchForm.status = undefined
  dateRange.value = undefined
  pagination.value.current = 1
  loadData()
}

function handleTableChange(pag: any) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

async function handleView(record: PrescriptionPageItemVO) {
  detail.value = null
  drawerVisible.value = true
  detailLoading.value = true
  try {
    const res = await props.detailApi(props.role, record.id)
    detail.value = res.data || null
  } catch {
    message.error('加载处方详情失败')
  } finally {
    detailLoading.value = false
  }
}

function getStatusText(status: number) {
  const map: Record<number, string> = { 0: '待支付', 1: '已支付', 2: '已发药', 3: '已取消' }
  return map[status] || '未知'
}

function getStatusColor(status: number) {
  const map: Record<number, string> = { 0: 'orange', 1: 'blue', 2: 'green', 3: 'default' }
  return map[status] || 'default'
}
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}

.data-table {
  margin-top: 16px;
}

.detail-desc {
  margin-bottom: 16px;
}

.section-title {
  margin: 0 0 12px;
  font-size: 16px;
  font-weight: 500;
}
</style>
