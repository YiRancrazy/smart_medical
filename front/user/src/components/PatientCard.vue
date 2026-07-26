<template>
  <div class="patient-card" @click="emit('click')">
    <div class="info">
      <div class="name">{{ name }} <span class="relation">{{ relation }}</span></div>
      <div class="id-card">{{ maskedIdCard }}</div>
    </div>
    <van-tag v-if="isDefaulted" type="success" size="medium">默认</van-tag>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  name: string
  relation?: string
  idCard?: string
  isDefaulted?: boolean
}>()

const emit = defineEmits<{
  click: []
}>()

const maskedIdCard = computed(() => {
  if (!props.idCard || props.idCard.length < 8) return props.idCard || ''
  return props.idCard.slice(0, 4) + '********' + props.idCard.slice(-4)
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.patient-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: $color-bg-card-solid;
  border-radius: $radius-lg;
  margin-bottom: 12px;
  box-shadow: $shadow-sm;
}

.name {
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
}

.relation {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  font-weight: $font-weight-normal;
}

.id-card {
  margin-top: 4px;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}
</style>
