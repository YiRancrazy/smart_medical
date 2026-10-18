<template>
  <van-tag :type="tagType">{{ label }}</van-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  status: number
  type?: 'registration' | 'prescription'
}>(), {
  type: 'registration'
})

const registrationMap: Record<number, { type: any; label: string }> = {
  0: { type: 'warning', label: '待支付' },
  1: { type: 'primary', label: '待就诊' },
  2: { type: 'danger', label: '支付失败' },
  3: { type: 'default', label: '已取消' },
  4: { type: 'success', label: '已完成' },
  5: { type: 'primary', label: '已报到' },
  6: { type: 'primary', label: '就诊中' },
  7: { type: 'warning', label: '待支付' }
}

const prescriptionMap: Record<number, { type: any; label: string }> = {
  0: { type: 'warning', label: '待支付' },
  1: { type: 'primary', label: '已支付' },
  2: { type: 'success', label: '已发药' },
  3: { type: 'default', label: '已取消' }
}

const tagType = computed(() => {
  const map = props.type === 'prescription' ? prescriptionMap : registrationMap
  return map[props.status]?.type || 'default'
})
const label = computed(() => {
  const map = props.type === 'prescription' ? prescriptionMap : registrationMap
  return map[props.status]?.label || '未知'
})
</script>
