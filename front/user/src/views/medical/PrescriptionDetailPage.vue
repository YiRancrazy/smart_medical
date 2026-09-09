<template>
  <div class="page" :class="{ 'has-action-bar': detail && detail.status === 0 }">
    <van-nav-bar title="处方详情" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" size="24px" />
    <empty-state v-else-if="!detail" description="暂无处方信息" />
    <template v-else>
      <glass-card class="card">
        <div class="section-title">处方信息</div>
        <van-cell title="患者" :value="detail.patientName" />
        <van-cell title="医生" :value="detail.doctorName" />
        <van-cell title="科室" :value="detail.departmentName" />
        <van-cell title="状态">
          <template #value>
            <status-tag :status="detail.status" type="prescription" />
          </template>
        </van-cell>
        <van-cell title="总金额" :value="formatMoney(detail.totalAmount)" />
      </glass-card>

      <glass-card class="card">
        <div class="section-title">药品清单</div>
        <van-cell
          v-for="item in detail.items"
          :key="item.drugId"
          :title="item.drugName"
          :label="item.usageMethod"
        >
          <template #value>
            <div class="item-value">
              <div>×{{ item.quantity }}</div>
              <div class="price">{{ formatMoney(item.unitPrice) }}</div>
            </div>
          </template>
        </van-cell>
      </glass-card>
    </template>

    <!-- 待支付：底部支付入口 -->
    <van-action-bar v-if="detail && detail.status === 0">
      <van-action-bar-button
        type="primary"
        text="立即支付"
        :disabled="!detail.orderId"
        @click="handlePay"
      />
    </van-action-bar>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPrescriptionDetail } from '@/api/prescription'
import type { PrescriptionDetailVO } from '@/api/prescription'
import GlassCard from '@/components/GlassCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import { formatMoney } from '@/utils/format'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()
const loading = ref(true)
const detail = ref<PrescriptionDetailVO | null>(null)
const id = String(route.params.id)

onMounted(async () => {
  try {
    const res = await getPrescriptionDetail(id)
    detail.value = res.data || null
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
})

/**
 * 跳转统一支付页（type=drug 走药品费支付）
 */
function handlePay() {
  if (!detail.value?.orderId) {
    showToast('处方暂无可支付订单')
    return
  }
  router.push({
    path: '/registration/payment',
    query: {
      orderId: detail.value.orderId,
      amount: detail.value.totalAmount,
      type: 'drug',
      from: 'prescription'
    }
  })
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $color-bg-page;
  padding-bottom: 24px;
}

/* action-bar 底部固定，页面预留高度防止遮挡内容 */
.has-action-bar {
  padding-bottom: 60px;
}

.card {
  margin: 16px;
}

.section-title {
  font-size: $font-size-h3;
  font-weight: $font-weight-semibold;
  margin-bottom: 12px;
  color: $color-text-primary;
}

.item-value {
  text-align: right;
  font-size: 13px;
  color: $color-text-secondary;

  .price {
    color: #f56c6c;
    font-weight: 500;
    margin-top: 2px;
  }
}
</style>
