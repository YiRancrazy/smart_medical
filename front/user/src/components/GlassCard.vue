<template>
  <div class="glass-card" :style="{ padding, borderRadius: radius }">
    <slot />
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  padding?: string
  radius?: string
}>(), {
  padding: '16px',
  radius: '16px'
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/glassmorphism.scss';

.glass-card {
  @include glass-card;
}
</style>
