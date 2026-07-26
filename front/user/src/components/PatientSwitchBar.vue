<template>
  <div v-if="visible" class="patient-switch-bar" @click="onClickBar">
    <div class="content">
      <van-icon name="user-o" class="icon-user" />
      <span class="text">{{ displayText }}</span>
      <van-icon v-if="hasPatients" name="arrow-down" class="icon-arrow" />
      <van-icon v-else name="arrow" class="icon-arrow" />
    </div>
  </div>

  <van-action-sheet
    v-model:show="showSheet"
    :actions="actions"
    title="切换就诊人"
    close-on-click-action
    @select="onSelect"
  />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePatientStore } from '@/stores/patient'
import type { ActionSheetAction } from 'vant'

// U24: 扩展 Vant Action 类型，承载 patientCardId（运行时 OK，类型也安全）
interface PatientAction extends ActionSheetAction {
  patientCardId: string
}

const route = useRoute()
const router = useRouter()
const patientStore = usePatientStore()

const visible = computed(() => {
  const meta = route.meta || {}
  return meta.requiresAuth !== false && route.path !== '/login'
})

const displayText = computed(() => {
  if (!patientStore.hasPatients) return '未选择就诊人，点击添加'
  if (!patientStore.currentPatient) return '未选择就诊人'
  const p = patientStore.currentPatient
  return `${p.patientName} (${p.relation || '本人'})`
})

const hasPatients = computed(() => patientStore.hasPatients)

const showSheet = ref(false)

const actions = computed<PatientAction[]>(() => {
  const list = patientStore.patientList.map(p => ({
    name: `${p.patientName} (${p.relation || '本人'})`,
    color: p.patientCardId === patientStore.selectedPatientCardId ? '#1989fa' : '',
    patientCardId: p.patientCardId
  }))
  list.push({ name: '管理就诊人', color: '#969799', patientCardId: '__manage__' })
  return list
})

watch(visible, (val) => {
  if (val) {
    patientStore.init()
  }
}, { immediate: true })

function onClickBar() {
  if (!patientStore.hasPatients) {
    router.push('/patient/edit')
    return
  }
  showSheet.value = true
}

function onSelect(action: PatientAction) {
  if (action.patientCardId === '__manage__') {
    router.push('/patient')
    return
  }
  const patient = patientStore.patientList.find(p => p.patientCardId === action.patientCardId)
  if (patient) {
    patientStore.selectPatient(patient)
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.patient-switch-bar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  padding: 0 16px;
  background: $color-bg-card;
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);

  .content {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: $font-size-body;
    color: $color-text-primary;
  }

  .text {
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .icon-user {
    color: $color-primary;
  }

  .icon-arrow {
    font-size: 12px;
    color: $color-text-secondary;
  }
}
</style>
