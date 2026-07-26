<template>
  <page-container title="处方详情">
    <glass-card>
      <a-space class="search-bar">
        <!-- F15: a-input-number 内部用 JS number 无法承载19位雪花ID，改 a-input + string -->
        <a-input
          v-model:value="prescriptionId"
          placeholder="请输入处方ID"
          style="width: 200px"
        />
        <a-button type="primary" @click="handleSearch" :loading="searchLoading">查询</a-button>
      </a-space>

      <template v-if="prescriptionDetail">
        <a-descriptions :column="2" bordered size="small" class="detail-desc">
          <a-descriptions-item
            v-for="key in displayKeys"
            :key="key"
            :label="key"
          >
            {{ formatValue(key, prescriptionDetail[key]) }}
          </a-descriptions-item>
        </a-descriptions>

        <a-space class="action-bar" v-if="canDispense">
          <a-button type="primary" @click="handleDispense">发药</a-button>
        </a-space>
      </template>

      <a-empty v-else-if="searched" description="未查询到处方信息" />
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
import { ref, computed } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import { getPrescription, dispense } from '@/api/pharmacy/prescription'
import type { DispenseVO } from '@/api/pharmacy/prescription'
import { message } from 'ant-design-vue'
import Modal from 'ant-design-vue/es/modal'

/**
 * 处方详情页面
 * @Author: YiRanCrazy@gmail.com
 * @Description: 根据处方ID查询详情，支持发药操作
 * @Datetime: 2026-07-18 10:00
 * @Version: 1.0
 */

const prescriptionId = ref<string | undefined>(undefined)
const searchLoading = ref(false)
const searched = ref(false)
const prescriptionDetail = ref<Record<string, any> | null>(null)
const dispenseResultVisible = ref(false)
const dispenseResult = ref<DispenseVO | null>(null)

/** 需要在描述列表中展示的字段（过滤掉内部字段） */
const HIDDEN_KEYS = new Set(['createTime', 'updateTime', 'isDeleted', 'class'])
const displayKeys = computed(() => {
  if (!prescriptionDetail.value) return []
  return Object.keys(prescriptionDetail.value).filter(k => !HIDDEN_KEYS.has(k))
})

/** 仅已支付=1 时显示发药按钮 */
const canDispense = computed(() => {
  if (!prescriptionDetail.value) return false
  const status = prescriptionDetail.value.status ?? prescriptionDetail.value.prescriptionStatus
  return Number(status) === 1
})

const itemColumns = [
  { title: '药品ID', dataIndex: 'drugId', key: 'drugId', width: 100 },
  { title: '药品名称', dataIndex: 'drugName', key: 'drugName' },
  { title: '数量', dataIndex: 'quantity', key: 'quantity', width: 80 },
  { title: '发药后库存', dataIndex: 'stockAfter', key: 'stockAfter', width: 120 }
]

async function handleSearch() {
  if (!prescriptionId.value) {
    message.warning('请输入处方ID')
    return
  }
  searchLoading.value = true
  searched.value = false
  try {
    const res = await getPrescription(prescriptionId.value)
    prescriptionDetail.value = res.data || null
    searched.value = true
  } catch (error) {
    message.error('查询处方详情失败')
    prescriptionDetail.value = null
    searched.value = true
  } finally {
    searchLoading.value = false
  }
}

function formatDate(val: string) {
  if (!val) return '-'
  return val.replace('T', ' ').substring(0, 19)
}

function formatValue(key: string, val: any) {
  if (val === null || val === undefined) return '-'
  if (key === 'totalAmount' && typeof val === 'number') return (val / 100).toFixed(2) + '元'
  // ISO 8601 日期格式判断（YYYY-MM-DDTHH:mm:ss）
  if (typeof val === 'string' && /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/.test(val)) return formatDate(val)
  return String(val)
}

function handleDispense() {
  if (!prescriptionId.value) return
  const id = prescriptionId.value
  Modal.confirm({
    title: '确认发药',
    content: `确定对处方 ${id} 执行发药操作吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        const res = await dispense(id)
        message.success('发药成功')
        dispenseResult.value = res.data
        dispenseResultVisible.value = true
        handleSearch()
      } catch (error) {
        message.error('发药失败')
      }
    }
  })
}
</script>

<style scoped>
.search-bar {
  margin-bottom: 16px;
}

.detail-desc {
  margin-top: 16px;
}

.action-bar {
  margin-top: 16px;
}

.result-desc {
  margin-bottom: 16px;
}

.item-table {
  margin-top: 8px;
}
</style>
