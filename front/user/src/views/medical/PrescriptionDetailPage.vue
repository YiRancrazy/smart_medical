<template>
  <div class="page">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPrescriptionDetail } from '@/api/prescription'
import type { PrescriptionDetailVO } from '@/api/prescription'
import GlassCard from '@/components/GlassCard.vue'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import { formatMoney } from '@/utils/format'
import { showToast } from 'vant'

const route = useRoute()
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
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $color-bg-page;
  padding-bottom: 24px;
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
