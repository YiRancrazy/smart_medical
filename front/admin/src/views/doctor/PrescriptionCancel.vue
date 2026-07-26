<template>
  <page-container title="处方作废">
    <glass-card>
      <a-form layout="inline" style="margin-bottom: 24px">
        <a-form-item label="处方ID">
          <!-- F15: a-input-number 内部用 JS number 无法承载19位雪花ID，改 a-input + string -->
          <a-input v-model:value="prescriptionId" placeholder="请输入处方ID" style="width: 200px" />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" danger :disabled="!prescriptionId" @click="handleCancel">作废处方</a-button>
        </a-form-item>
      </a-form>

      <a-alert
        message="仅状态为「待支付」的处方可以作废，作废后不可恢复"
        type="warning"
        show-icon
        style="margin-bottom: 16px"
      />
    </glass-card>
  </page-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import { cancelPrescription } from '@/api/doctor/prescription'
import { message } from 'ant-design-vue'
import Modal from 'ant-design-vue/es/modal'

const prescriptionId = ref<string | undefined>(undefined)

function handleCancel() {
  if (!prescriptionId.value) return
  const id = prescriptionId.value
  Modal.confirm({
    title: '确认作废',
    content: `确定要作废处方 #${id} 吗？此操作不可恢复。`,
    okText: '确定作废',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await cancelPrescription(id)
        message.success('处方已作废')
        prescriptionId.value = undefined
      } catch {
        message.error('作废失败')
      }
    }
  })
}
</script>
