<template>
  <page-container title="库存预警">
    <glass-card>
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        row-key="id"
        class="data-table"
        :row-class-name="getRowClassName"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'shortage'">
            <span :style="{ color: record.shortage > 0 ? '#ff4d4f' : undefined, fontWeight: record.shortage > 0 ? 'bold' : undefined }">
              {{ record.shortage }}
            </span>
          </template>
          <template v-if="column.key === 'updateTime'">
            {{ formatDate(record.updateTime) }}
          </template>
        </template>
      </a-table>
    </glass-card>
  </page-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import { getLowStockList } from '@/api/pharmacy/inventory'
import type { DrugInventory } from '@/api/pharmacy/inventory'
import { message } from 'ant-design-vue'

/**
 * 库存预警页面
 * @Author: YiRanCrazy@gmail.com
 * @Description: 展示低于最低库存量的药品列表
 * @Datetime: 2026-07-18 10:00
 * @Version: 1.0
 */

const tableData = ref<DrugInventory[]>([])
const loading = ref(false)

const columns = [
  { title: '药品ID', dataIndex: 'drugId', key: 'drugId', width: 120 },
  { title: '药品名称', dataIndex: 'drugName', key: 'drugName' },
  { title: '当前库存', dataIndex: 'currentStock', key: 'currentStock', width: 120 },
  { title: '最低库存', dataIndex: 'minStock', key: 'minStock', width: 120 },
  { title: '缺口数量', dataIndex: 'shortage', key: 'shortage', width: 120 },
  { title: '更新时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 }
]

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getLowStockList()
    tableData.value = res.data || []
  } catch (error) {
    message.error('加载库存预警列表失败')
  } finally {
    loading.value = false
  }
}

function formatDate(val: string) {
  if (!val) return '-'
  return val.replace('T', ' ').substring(0, 19)
}

/** 当前库存低于最低库存时行背景标红 */
function getRowClassName(record: DrugInventory) {
  return record.currentStock < record.minStock ? 'row-alert' : ''
}
</script>

<style scoped>
.data-table {
  margin-top: 0;
}
</style>

<style>
/* 行标红需全局样式才生效 */
.row-alert {
  background-color: #fff1f0 !important;
}
.row-alert:hover > td {
  background-color: #ffccc7 !important;
}
</style>
