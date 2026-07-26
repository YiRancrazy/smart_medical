<template>
  <a-modal
    v-model:open="visible"
    :title="title"
    :ok-text="okText"
    @ok="handleOk"
  >
    <p>{{ message }}</p>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { getBizErrorMessage } from '@/utils/bizCode'

/**
 * 业务错误提示
 * @Author: YiRanCrazy@gmail.com
 * @Description: 针对业务错误码的友好提示，支持并发冲突的特殊处理
 * @Datetime: 2026-07-17 11:40
 * @Version: 1.0
 */

const props = defineProps<{
  code?: number
  msg?: string
}>()

const visible = ref(false)
const title = ref('提示')
const message = ref('')
const okText = ref('确定')

watch(() => props.code, (newCode) => {
  if (newCode) {
    const displayMsg = getBizErrorMessage(newCode, props.msg || '')
    message.value = displayMsg

    // 并发冲突特殊处理
    if (newCode === 7001) {
      title.value = '操作冲突'
      okText.value = '刷新页面'
    } else {
      title.value = '操作失败'
      okText.value = '确定'
    }
    visible.value = true
  }
})

function handleOk() {
  if (props.code === 7001) {
    window.location.reload()
  } else {
    visible.value = false
  }
}

defineExpose({
  show: (code: number, msg: string) => {
    message.value = getBizErrorMessage(code, msg)
    visible.value = true
  }
})
</script>