<template>
  <div class="page">
    <van-nav-bar title="就诊人管理" left-arrow @click-left="$router.back()">
      <template #right>
        <van-icon name="plus" size="18" @click="$router.push('/patient/edit')" />
      </template>
    </van-nav-bar>

    <van-loading v-if="patientStore.loading" size="24px" />
    <empty-state v-else-if="!patientStore.patientList.length" description="暂无就诊人" />
    <div v-else class="list">
      <van-swipe-cell v-for="p in patientStore.patientList" :key="p.userPatientRelationId">
        <van-cell :title="p.patientName" :label="p.relation" is-link @click="goEdit(p)">
          <template #icon>
            <van-icon name="user-o" style="margin-right: 8px" />
          </template>
          <template #right-icon>
            <van-tag v-if="isCurrent(p)" type="success" style="margin-right: 8px">当前</van-tag>
            <van-tag v-if="p.defaultPatient" type="primary">默认</van-tag>
          </template>
        </van-cell>
        <template #right>
          <van-button square type="success" text="切换" @click="onSwitch(p)" />
          <van-button square type="primary" text="默认" @click="onSetDefault(p)" />
          <van-button square type="danger" text="删除" @click="onDelete(p)" />
        </template>
      </van-swipe-cell>
    </div>

    <van-button round block type="primary" class="add-btn" @click="$router.push('/patient/edit')">添加就诊人</van-button>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { setDefaultPatient, deletePatient } from '@/api/patient'
import type { PatientCardSimpleResponse } from '@/api/patient'
import { usePatientStore } from '@/stores/patient'
import EmptyState from '@/components/EmptyState.vue'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()
const patientStore = usePatientStore()

function isCurrent(p: PatientCardSimpleResponse) {
  return p.patientCardId === patientStore.selectedPatientCardId
}

async function onSwitch(p: PatientCardSimpleResponse) {
  patientStore.selectPatient(p)
  showToast(`已切换为 ${p.patientName}`)
}

// U07: 直接用 store 加载，编辑/删除返回后 store 已是最新，无需本地 list
onMounted(() => patientStore.loadPatients())

function goEdit(p: PatientCardSimpleResponse) {
  router.push({ path: '/patient/edit', query: { id: p.userPatientRelationId } })
}

async function onSetDefault(p: PatientCardSimpleResponse) {
  try {
    await setDefaultPatient(p.userPatientRelationId)
    showToast('已设为默认')
    await patientStore.loadPatients()
  } catch {
    showToast('设置失败')
  }
}

async function onDelete(p: PatientCardSimpleResponse) {
  // F26: 拆两段 try——showConfirmDialog 抛错=用户取消(静默 return)，deletePatient 抛错=请求失败(需提示)
  try {
    await showConfirmDialog({ title: '确认删除', message: `确定删除就诊人${p.patientName}吗？` })
  } catch {
    return // 用户点取消
  }
  try {
    await deletePatient(p.userPatientRelationId)
    showToast('已删除')
    // U10: 刷新 store 触发 ensureSelected，若删的正是选中就诊人会自动切到下一个
    await patientStore.loadPatients()
  } catch {
    showToast('删除失败，请重试')
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $color-bg-page;
  padding-bottom: 80px;
}

.list {
  margin: 12px 0;
}

.add-btn {
  position: fixed;
  bottom: 24px;
  left: 16px;
  right: 16px;
  width: auto;
}
</style>
