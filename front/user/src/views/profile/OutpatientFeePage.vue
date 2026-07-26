<template>
  <div class="page">
    <van-nav-bar title="门诊费用" left-arrow @click-left="$router.back()" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <van-cell-group inset>
          <van-cell v-for="item in list" :key="item.orderId">
            <template #title>
              <div class="cell-title">
                <span class="type">{{ item.orderTypeName }}</span>
                <van-tag :type="statusTagType(item.status)">{{ item.statusName }}</van-tag>
              </div>
            </template>
            <template #label>
              <div class="cell-label">
                <div>单号 {{ item.orderSn }}</div>
                <div>{{ formatDate(item.createTime) }}</div>
              </div>
            </template>
            <template #value>
              <div class="cell-value">
                <div class="amount">{{ formatMoney(item.totalAmount) }}</div>
                <van-button
                  v-if="item.status === 0"
                  size="small"
                  type="primary"
                  round
                  @click.stop="handlePay(item)"
                >去支付</van-button>
              </div>
            </template>
          </van-cell>
        </van-cell-group>
      </van-list>
    </van-pull-refresh>

    <empty-state v-if="!loading && !list.length" description="暂无门诊费用记录" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOutpatientFeeList, type OutpatientFeeItem } from '@/api/outpatientFee'
import EmptyState from '@/components/EmptyState.vue'
import { formatDate, formatMoney } from '@/utils/format'
import { showToast } from 'vant'

const router = useRouter()

const list = ref<OutpatientFeeItem[]>([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const pageNum = ref(1)
const pageSize = 10

onMounted(() => {
  onLoad()
})

async function onLoad() {
  loading.value = true
  try {
    const res = await getOutpatientFeeList({ current: pageNum.value, size: pageSize })
    const data = res.data
    if (refreshing.value) {
      list.value = []
      refreshing.value = false
    }
    if (data?.list) {
      list.value.push(...data.list)
    }
    finished.value = !data || data.list.length < pageSize || pageNum.value >= (data.totalPages || 1)
    pageNum.value++
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
}

async function onRefresh() {
  pageNum.value = 1
  finished.value = false
  refreshing.value = true
  await onLoad()
}

async function handlePay(item: OutpatientFeeItem) {
  // 跳转到统一支付页选择支付方式
  router.push(`/registration/payment?orderId=${item.orderId}&amount=${item.totalAmount}&type=drug`)
}

function statusTagType(status: number) {
  switch (status) {
    case 0:
      return 'warning'
    case 1:
      return 'primary'
    case 3:
      return 'success'
    default:
      return 'default'
  }
}
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.cell-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;

  .type {
    color: $color-text-primary;
  }
}

.cell-label {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.6;
}

.cell-value {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;

  .amount {
    color: #f56c6c;
    font-weight: 500;
    font-size: 14px;
  }
}
</style>
