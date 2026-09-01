<template>
  <page-container title="待发药">
    <glass-card>
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        row-key="prescriptionId"
        class="data-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'totalAmount'">
            {{ (record.totalAmount / 100).toFixed(2) }}元
          </template>
          <template v-if="column.key === 'createdAt'">
            {{ formatDate(record.createdAt) }}
          </template>
          <template v-if="column.key === 'action'">
            <a-button type="primary" size="small" @click="handleDispense(record)">发药</a-button>
          </template>
        </template>
      </a-table>
    </glass-card>

    <!-- 发药结果弹窗 -->
    <a-modal
      v-model:open="dispenseResultVisible"
      title="发药结果"
      :footer="null"
      width="600px"
    >
      <template v-if="dispenseResult">
        <a-descriptions :column="1" bordered size="small" class="result-desc">
          <a-descriptions-item label="处方ID">{{ dispenseResult.prescriptionId }}</a-descriptions-item>
          <a-descriptions-item label="处方状态">{{ dispenseResult.prescriptionStatus }}</a-descriptions-item>
          <a-descriptions-item label="发药时间">{{ formatDate(dispenseResult.dispensedAt) }}</a-descriptions-item>
        </a-descriptions>
        <a-table
          :columns="itemColumns"
          :data-source="dispenseResult.items"
          row-key="drugId"
          size="small"
          class="item-table"
          :pagination="false"
        />
      </template>
    </a-modal>
  </page-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import { getPendingList, dispense } from '@/api/pharmacy/prescription'
import type { PendingPrescriptionVO, DispenseVO } from '@/api/pharmacy/prescription'
import { message } from 'ant-design-vue'
import Modal from 'ant-design-vue/es/modal'

/**
 * 待发药列表页面
 * @Author: YiRanCrazy@gmail.com
 * @Description: 展示待发药处方列表，支持发药操作
 * @Datetime: 2026-07-18 10:00
 * @Version: 1.0
 */

const tableData = ref<PendingPrescriptionVO[]>([])
const loading = ref(false)
const dispenseResultVisible = ref(false)
const dispenseResult = ref<DispenseVO | null>(null)

const columns = [
  { title: '处方ID', dataIndex: 'prescriptionId', key: 'prescriptionId', width: 120 },
  { title: '订单ID', dataIndex: 'orderId', key: 'orderId', width: 120 },
  { title: '患者ID', dataIndex: 'patientId', key: 'patientId', width: 120 },
  { title: '挂号记录ID', dataIndex: 'registrationSn', key: 'registrationSn', width: 140 },
  { title: '总金额', dataIndex: 'totalAmount', key: 'totalAmount', width: 120 },
  { title: '创建时间', dataIndex: 'createdAt', key: 'createdAt', width: 180 },
  { title: '操作', key: 'action', width: 100, fixed: 'right' }
]

const itemColumns = [
  { title: '药品ID', dataIndex: 'drugId', key: 'drugId', width: 100 },
  { title: '药品名称', dataIndex: 'drugName', key: 'drugName' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 80 },
  { title: '发药后库存', dataIndex: 'stockAfter', key: 'stockAfter', width: 120 }
]

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getPendingList()
    tableData.value = res.data?.list || []
  } catch (error: any) {
    message.error(error.message || '加载待发药列表失败')
  } finally {
    loading.value = false
  }
}

function formatDate(val: string) {
  if (!val) return '-'
  return val.replace('T', ' ').substring(0, 19)
}

function handleDispense(record: PendingPrescriptionVO) {
  Modal.confirm({
    title: '确认发药',
    content: `确定对处方 ${record.prescriptionId} 执行发药操作吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await dispense(record.prescriptionId)
        message.success('发药成功')
        dispenseResult.value = res.data
        dispenseResultVisible.value = true
        loadData()
      } catch (error: any) {
        message.error(error.message || '发药失败')
        loadData()
      }
    }
  })
}
</script>

<style scoped>
.data-table {
  margin-top: 0;
}

.result-desc {
  margin-bottom: 16px;
}

.item-table {
  margin-top: 8px;
}
</style>
