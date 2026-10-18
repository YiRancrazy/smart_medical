<template>
  <div class="empty-state">
    <van-empty :description="description" :image="image" />
  </div>
</template>

<script setup lang="ts">
defineProps<{
  description?: string
  image?: string
}>()
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.empty-state {
  padding: 32px 0;
}
</style>
