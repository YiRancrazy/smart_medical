<template>
  <div class="page">
    <van-nav-bar title="支付" left-arrow @click-left="$router.back()" />

    <!-- 金额 -->
    <glass-card class="card">
      <div class="amount">{{ formatMoney(amount) }}</div>
      <div class="desc">{{ feeDesc }}</div>
    </glass-card>

    <!-- 支付方式 -->
    <glass-card class="card">
      <div class="section-title">支付方式</div>
      <van-loading v-if="methodLoading" size="24px" />
      <van-radio-group v-else v-model="selectedMethodId">
        <van-cell-group inset>
          <van-cell
            v-for="m in methods"
            :key="m.id"
            :title="m.name"
            clickable
            @click="selectedMethodId = m.id"
          >
            <template #right-icon>
              <van-radio :name="m.id" />
            </template>
          </van-cell>
        </van-cell-group>
      </van-radio-group>
    </glass-card>

    <!-- 底部 -->
    <van-action-bar>
      <van-action-bar-button
        type="primary"
        text="确认支付"
        :loading="paying"
        :disabled="!selectedMethodId"
        @click="handlePay"
      />
    </van-action-bar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import GlassCard from '@/components/GlassCard.vue'
import { getAllPaymentMethods, getDefaultPaymentMethod, pay } from '@/api/payment'
import type { PaymentMethod } from '@/api/payment'
import { useRegistrationStore } from '@/stores/registration'
import { formatMoney } from '@/utils/format'
import { showToast } from 'vant'

/**
 * 支付页面
 * @Author: YiRanCrazy@gmail.com
 * @Description: 选择支付方式、确认支付
 * @Datetime: 2026-07-18 10:00
 * @Version: 1.0
 */

const router = useRouter()
const route = useRoute()
const regStore = useRegistrationStore()

const amount = computed(() => Number(route.query.amount) || (regStore.selectedSchedule as any)?.price || 0)

const feeDesc = computed(() => {
  const type = route.query.type as string
  if (type === 'drug') return '药品费'
  if (type === 'registration') return '挂号费'
  return '订单费用'
})

const methodLoading = ref(false)
const methods = ref<PaymentMethod[]>([])
const selectedMethodId = ref<number | null>(null)

const paying = ref(false)

onMounted(async () => {
  await Promise.all([loadMethods(), loadDefaultMethod()])
})

async function loadMethods() {
  methodLoading.value = true
  try {
    const res = await getAllPaymentMethods()
    methods.value = res.data || []
  } catch {
    showToast('加载支付方式失败')
  } finally {
    methodLoading.value = false
  }
}

async function loadDefaultMethod() {
  try {
    const res = await getDefaultPaymentMethod()
    if (res.data?.id) {
      selectedMethodId.value = Number(res.data.id)
    }
  } catch {
    // 无默认则不选
  }
}

async function handlePay() {
  const orderId = route.query.orderId || regStore.orderId
  if (!orderId) {
    showToast('缺少订单信息')
    return
  }
  // U25: 显式守卫替代非空断言
  if (!selectedMethodId.value) {
    showToast('请选择支付方式')
    return
  }
  paying.value = true
  try {
    // U29: realAmount 后端为 Integer（分），Math.round 确保整数避免 400
    await pay({ orderId: String(orderId), paymentMethodId: selectedMethodId.value, realAmount: Math.round(amount.value) })
    showToast('支付成功')
    regStore.resetFlow()
    // 支付成功后返回来源页：药品订单回门诊费用，挂号订单回挂号列表
    const backPath = route.query.type === 'drug' ? '/outpatient-fee' : '/registration'
    router.replace(backPath)
  } catch {
    // M20: 业务错误已在拦截器统一提示，避免二次 toast
  } finally {
    paying.value = false
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $color-bg-page;
  padding-bottom: 60px;
}

.card {
  margin: 16px;
}

.amount {
  font-size: 40px;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  text-align: center;
}

.desc {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  text-align: center;
  margin-top: 8px;
}

.section-title {
  font-size: $font-size-h3;
  font-weight: $font-weight-semibold;
  margin-bottom: 12px;
  color: $color-text-primary;
}
</style>
