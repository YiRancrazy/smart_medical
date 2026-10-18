<template>
  <div class="page-container">
    <div v-if="title || subTitle || $slots.extra" class="page-container__header">
      <div>
        <h2 class="page-container__title">{{ title }}</h2>
        <p v-if="subTitle" class="page-container__subtitle">{{ subTitle }}</p>
      </div>
      <div class="page-container__extra">
        <slot name="extra" />
      </div>
    </div>
    <div class="page-container__content">
      <slot />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title?: string
  subTitle?: string
}>()
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.page-container {
  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: @spacing-lg;
  }

  &__title {
    margin: 0;
    font-size: @font-size-2xl;
    font-weight: @font-weight-semibold;
    color: @text-color;
  }

  &__subtitle {
    margin: @spacing-xs 0 0;
    font-size: @font-size-base;
    color: @text-color-secondary;
  }

  &__content {
    display: flex;
    flex-direction: column;
    gap: @spacing-lg;
  }
}
</style>
