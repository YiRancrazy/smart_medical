<template>
  <div class="reg-list">
    <van-empty v-if="!list.length" description="暂无记录" />
    <div v-for="reg in list" :key="reg.id" class="reg-item">
      <registration-card
        :doctor-name="reg.doctorName"
        :department-name="reg.departmentName"
        :patient-name="reg.patientName"
        :visit-date="reg.scheduleDate"
        :start-time="reg.scheduleTime"
        :status="reg.status"
        :price="reg.registrationPrice"
      />
      <!-- U11: status ∈ {0,1,2,7} 才有按钮，否则隐藏 actions 避免空 div 占位 -->
      <div v-if="showActions && [0, 1, 2, 7].includes(reg.status)" class="actions">
        <!-- U02/U11: 状态0挂号费待支付、状态2支付失败、状态7处方补缴待支付 均提供去支付入口 -->
        <van-button v-if="[0, 2, 7].includes(reg.status)" size="small" type="primary" @click="emit('pay', reg)">去支付</van-button>
        <van-button v-if="reg.status === 1" size="small" type="primary" @click="emit('checkin', reg.id)">报到</van-button>
        <van-button v-if="[0, 1].includes(reg.status)" size="small" plain type="warning" @click="emit('cancel', reg.id)">取消</van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import RegistrationCard from '@/components/RegistrationCard.vue'
import type { AppointmentResponseSimple } from '@/api/registration'

defineProps<{
  list: AppointmentResponseSimple[]
  showActions?: boolean
}>()

const emit = defineEmits<{
  checkin: [id: string]
  cancel: [id: string]
  pay: [reg: AppointmentResponseSimple]
}>()
</script>

<style scoped lang="scss">
.reg-list {
  padding: 12px 16px;
}

.reg-item {
  margin-bottom: 12px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}
</style>
