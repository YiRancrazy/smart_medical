<template>
  <a-tag :color="colorMap[status] ?? 'default'">
    {{ labelMap[status] ?? '未知' }}
  </a-tag>
</template>

<script setup lang="ts">
/**
 * 挂号状态标签
 * @Author: YiRanCrazy@gmail.com
 * @Description: 根据状态码显示不同颜色的标签
 * @Datetime: 2026-07-17 11:35
 * @Version: 1.1
 */

import { registrationColorMap as colorMap, registrationLabelMap as labelMap } from './statusConfig'

defineProps<{
  status: number
}>()
</script>
