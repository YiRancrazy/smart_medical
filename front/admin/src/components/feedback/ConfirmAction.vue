<template>
  <a-modal
    v-model:open="visible"
    :title="title"
    :ok-text="okText"
    :cancel-text="cancelText"
    :ok-button-props="{ danger: danger }"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <p>{{ content }}</p>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'

/**
 * 操作确认弹窗
 * @Author: YiRanCrazy@gmail.com
 * @Description: 统一的操作确认弹窗，支持危险操作标识
 * @Datetime: 2026-07-17 11:42
 * @Version: 1.0
 */

withDefaults(defineProps<{
  title?: string
  content?: string
  okText?: string
  cancelText?: string
  danger?: boolean
}>(), {
  title: '确认操作',
  content: '确定要执行此操作吗？',
  okText: '确定',
  cancelText: '取消',
  danger: false
})

const emit = defineEmits<{
  (e: 'confirm'): void
  (e: 'cancel'): void
}>()

const visible = ref(false)

function handleConfirm() {
  emit('confirm')
  visible.value = false
}

function handleCancel() {
  emit('cancel')
  visible.value = false
}

function show() {
  visible.value = true
}

function hide() {
  visible.value = false
}

defineExpose({
  show,
  hide
})
</script>