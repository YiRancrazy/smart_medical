<template>
  <div class="registration-card" @click="emit('click')">
    <div class="header">
      <span class="title">{{ doctorName }} - {{ departmentName }}</span>
      <status-tag :status="status" />
    </div>
    <div class="body">
      <div class="info">就诊时间：{{ visitDate }} {{ startTime }}</div>
      <div class="info">就诊人：{{ patientName }}</div>
      <!-- U12: 待支付(0)/支付失败(2) 展示应付挂号费；处方补缴(7)应付的是处方费非 registrationPrice，不在此展示 -->
      <div v-if="price != null && [0, 2].includes(status)" class="info price">
        应付：{{ formatMoney(price) }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import StatusTag from './StatusTag.vue'
import { formatMoney } from '@/utils/format'

defineProps<{
  doctorName: string
  departmentName: string
  patientName: string
  visitDate: string
  startTime: string
  status: number
  price?: number
}>()

const emit = defineEmits<{
  click: []
}>()
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.registration-card {
  padding: 16px;
  background: $color-bg-card-solid;
  border-radius: $radius-lg;
  margin-bottom: 12px;
  box-shadow: $shadow-sm;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.title {
  font-size: $font-size-body;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
}

.info {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  margin-top: 4px;

  &.price {
    color: $color-primary;
    font-weight: $font-weight-medium;
  }
}
</style>
