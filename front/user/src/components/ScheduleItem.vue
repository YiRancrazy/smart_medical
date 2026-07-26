<template>
  <div class="schedule-item" @click="emit('click')">
    <div class="time">{{ date }} {{ startTime }}-{{ endTime }}</div>
    <div class="meta">
      <span class="price">{{ price != null ? formatMoney(price) : '-' }}</span>
      <span :class="['quota', remainQuota > 0 ? 'available' : 'none']">
        {{ remainQuota > 0 ? `剩余 ${remainQuota}` : '约满' }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { formatMoney } from '@/utils/format'

defineProps<{
  date: string
  startTime: string
  endTime: string
  price?: number
  remainQuota: number
}>()

const emit = defineEmits<{
  click: []
}>()
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.schedule-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: $color-bg-card-solid;
  border-radius: $radius-lg;
  margin-bottom: 12px;
  box-shadow: $shadow-sm;
}

.time {
  font-size: $font-size-body;
  color: $color-text-primary;
}

.meta {
  text-align: right;
}

.price {
  font-size: $font-size-body;
  font-weight: $font-weight-semibold;
  color: $color-danger;
  display: block;
}

.quota {
  font-size: $font-size-sm;
}

.available {
  color: $color-success;
}

.none {
  color: $color-text-disabled;
}
</style>
