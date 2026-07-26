<template>
  <div class="dept-card" @click="emit('click')">
    <div class="dept-name">{{ name }}</div>
    <div class="dept-meta">{{ count }} 位医生</div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  name: string
  count?: number
}>()

const emit = defineEmits<{
  click: []
}>()
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.dept-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  background: $color-bg-white;
  border-radius: $radius-lg;
  margin-bottom: 12px;
  box-shadow: $shadow-sm;
}

.dept-name {
  font-size: $font-size-body;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
}

.dept-meta {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}
</style>
