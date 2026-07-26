<template>
  <div class="glass-card" :class="[`glass-card--${variant}`]">
    <!-- F18: 支持 title prop 渲染卡片标题，兼容 header slot -->
    <div v-if="title || $slots.header" class="glass-card__header">
      <slot name="header">{{ title }}</slot>
    </div>
    <div class="glass-card__body">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  variant?: 'default' | 'elevated' | 'subtle'
  title?: string
}>()
</script>

<style scoped lang="less">
@import '@/styles/variables.less';
@import '@/styles/glassmorphism.less';

.glass-card {
  .glass-card();

  &--elevated {
    .glass-card-elevated();
  }

  &--subtle {
    .glass-card-subtle();
  }

  &__header {
    padding: @card-padding @card-padding 0;
    font-size: @font-size-lg;
    font-weight: @font-weight-semibold;
    color: @text-color;
  }

  &__body {
    padding: @card-padding;
  }
}
</style>
