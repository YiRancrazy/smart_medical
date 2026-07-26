<template>
  <div class="doctor-card" @click="emit('click')">
    <van-image round width="56px" height="56px" :src="avatar" class="avatar" />
    <div class="info">
      <div class="name">{{ name }} <span class="title">{{ title }}</span></div>
      <div class="tags">
        <van-tag v-for="tag in tagList" :key="tag" size="medium" type="primary" plain>{{ tag }}</van-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  name: string
  title?: string
  avatar?: string
  tags?: string
}>()

const emit = defineEmits<{
  click: []
}>()

const tagList = computed(() => {
  if (!props.tags) return []
  return props.tags.split(',').map((t) => t.trim()).filter(Boolean)
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.doctor-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background: $color-bg-white;
  border-radius: $radius-lg;
  margin-bottom: 12px;
  box-shadow: $shadow-sm;
}

.avatar {
  flex-shrink: 0;
  margin-right: 12px;
}

.name {
  font-size: $font-size-body;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
}

.title {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  font-weight: $font-weight-normal;
}

.tags {
  margin-top: 8px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
