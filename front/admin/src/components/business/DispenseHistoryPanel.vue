<template>
  <page-container :title="title">
    <glass-card>
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="患者姓名">
          <a-input v-model:value="searchForm.patientName" placeholder="请输入患者姓名" allow-clear />
        </a-form-item>
        <a-form-item label="发药人手机号">
          <a-input v-model:value="searchForm.dispenserPhone" placeholder="请输入发药人手机号" allow-clear />
        </a-form-item>
        <a-form-item label="处方ID">
          <a-input v-model:value="searchForm.prescriptionId" placeholder="请输入处方ID" allow-clear />
        </a-form-item>
        <a-form-item label="订单ID">
          <a-input v-model:value="searchForm.orderId" placeholder="请输入订单ID" allow-clear />
        </a-form-item>
        <a-form-item label="发药日期">
          <a-range-picker v-model:value="dateRange" />
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
        row-key="prescriptionId"
        class="data-table"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'totalAmount'">
            {{ formatMoney(record.totalAmount / 100) }}
          </template>
          <template v-if="column.key === 'status'">
            <a-tag color="green">已发药</a-tag>
          </template>
          <template v-if="column.key === 'dispensedAt'">
            {{ formatDateTime(record.dispensedAt) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
          </template>
        </template>
      </a-table>
    </glass-card>

    <a-drawer
      v-model:open="drawerVisible"
      title="发药记录详情"
      width="600px"
      :footer="null"
    >
      <a-spin :spinning="detailLoading">
        <a-descriptions v-if="detail" :column="1" bordered size="small">
          <a-descriptions-item label="处方ID">{{ detail.id }}</a-descriptions-item>
          <a-descriptions-item label="病历ID">{{ detail.medicalRecordId }}</a-descriptions-item>
          <a-descriptions-item label="患者姓名">{{ detail.patientName }}</a-descriptions-item>
          <a-descriptions-item label="患者手机号">{{ detail.patientPhone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="开方医生">{{ detail.doctorName || '-' }}</a-descriptions-item>
          <a-descriptions-item label="处方状态">
            <a-tag color="green">已发药</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="处方金额">{{ formatMoney(detail.totalAmount / 100) }}</a-descriptions-item>
          <a-descriptions-item label="发药人手机号">{{ currentRow?.dispenserPhone || '-' }}</a-descriptions-item>
          <a-descriptions-item label="发药时间">{{ formatDateTime(currentRow?.dispensedAt) }}</a-descriptions-item>
          <a-descriptions-item label="关联订单ID">{{ detail.orderId || '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建时间">{{ formatDateTime(detail.createTime) }}</a-descriptions-item>
        </a-descriptions>

        <h4 class="section-title">药品明细</h4>
        <a-table
          :columns="itemColumns"
          :data-source="detail?.items"
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
import { pageDispenseHistory } from '@/api/pharmacy/prescription'
import type { DispenseHistoryVO, DispenseHistoryQueryParams } from '@/api/pharmacy/prescription'
import { getPrescriptionDetail } from '@/api/history/prescription'
import type { PrescriptionDetailVO } from '@/api/history/prescription'
import { message } from 'ant-design-vue'

interface Props {
  title: string
}

defineProps<Props>()

const searchForm = reactive({
  patientName: '',
  dispenserPhone: '',
  prescriptionId: '',
  orderId: ''
})

const dateRange = ref<[Dayjs, Dayjs] | undefined>(undefined)

const tableData = ref<DispenseHistoryVO[]>([])
const loading = ref(false)
const drawerVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<PrescriptionDetailVO | null>(null)
const currentRow = ref<DispenseHistoryVO | null>(null)

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: '处方ID', dataIndex: 'prescriptionId', key: 'prescriptionId', width: 120 },
  { title: '订单ID', dataIndex: 'orderId', key: 'orderId', width: 120 },
  { title: '患者姓名', dataIndex: 'patientName', key: 'patientName', width: 120 },
  { title: '开方医生', dataIndex: 'doctorName', key: 'doctorName', width: 120 },
  { title: '发药人手机号', dataIndex: 'dispenserPhone', key: 'dispenserPhone', width: 140 },
  { title: '发药状态', dataIndex: 'status', key: 'status', width: 100 },
  { title: '处方金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 110 },
  { title: '药品项数', dataIndex: 'itemCount', key: 'itemCount', width: 100 },
  { title: '发药时间', dataIndex: 'dispensedAt', key: 'dispensedAt', width: 170 },
  { title: '操作', key: 'action', width: 90, fixed: 'right' }
]

const itemColumns = [
  { title: '药品名称', dataIndex: 'commonName', key: 'commonName' },
  { title: '规格', dataIndex: 'specification', key: 'specification', width: 120 },
  { title: '单位', dataIndex: 'unit', key: 'unit', width: 70 },
  { title: '单价', dataIndex: 'unitPrice', key: 'unitPrice', width: 100 },
  { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 70 },
  { title: '用法用量', dataIndex: 'usageMethod', key: 'usageMethod', ellipsis: true }
]

onMounted(() => {
  loadData()
})

const queryParams = computed<DispenseHistoryQueryParams>(() => {
  const params: DispenseHistoryQueryParams = {
    patientName: searchForm.patientName || undefined,
    dispenserPhone: searchForm.dispenserPhone || undefined,
    prescriptionId: searchForm.prescriptionId || undefined,
    orderId: searchForm.orderId || undefined,
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
    const res = await pageDispenseHistory(queryParams.value)
    if (res.data) {
      tableData.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch {
    message.error('加载发药历史失败')
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
  searchForm.dispenserPhone = ''
  searchForm.prescriptionId = ''
  searchForm.orderId = ''
  dateRange.value = undefined
  pagination.value.current = 1
  loadData()
}

function handleTableChange(pag: any) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

async function handleView(record: DispenseHistoryVO) {
  currentRow.value = record
  detail.value = null
  drawerVisible.value = true
  detailLoading.value = true
  try {
    const res = await getPrescriptionDetail('pharmacy', record.prescriptionId)
    detail.value = res.data || null
  } catch {
    message.error('加载发药记录详情失败')
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

.section-title {
  margin: 16px 0 12px;
  font-size: 16px;
  font-weight: 500;
}
</style>
