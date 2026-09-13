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
      <van-swipe-cell
        v-for="(p, i) in patientStore.patientList"
        :key="p.userPatientRelationId"
        :ref="el => setSwipeRef(el, i)"
        class="swipe-item"
        :class="{ 'is-active': activeKey === p.userPatientRelationId }"
        @open="onOpen(p.userPatientRelationId, i)"
        @close="onClose(p.userPatientRelationId)"
      >
        <van-cell
          class="patient-cell"
          :title="p.patientName"
          :label="p.relation"
          :border="false"
          @click="goEdit(p)"
        >
          <template #icon>
            <van-icon name="user-o" class="patient-icon" />
          </template>
          <template #right-icon>
            <van-tag v-if="isCurrent(p)" type="success" class="patient-tag">当前</van-tag>
            <van-tag v-if="p.defaultPatient" type="primary">默认</van-tag>
          </template>
        </van-cell>
        <template #right>
          <div class="actions">
            <button type="button" class="action action--switch" @click="onSwitch(p)">切换</button>
            <button type="button" class="action action--default" @click="onSetDefault(p)">默认</button>
            <button type="button" class="action action--delete" @click="onDelete(p)">删除</button>
          </div>
        </template>
      </van-swipe-cell>
    </div>

    <van-button round block type="primary" class="add-btn" @click="$router.push('/patient/edit')">添加就诊人</van-button>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { SwipeCellInstance } from 'vant'
import { setDefaultPatient, deletePatient } from '@/api/patient'
import type { PatientCardSimpleResponse } from '@/api/patient'
import { usePatientStore } from '@/stores/patient'
import EmptyState from '@/components/EmptyState.vue'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()
const patientStore = usePatientStore()

// 当前划开的项（存 relationId 而非下标，列表刷新后不会错位）
const activeKey = ref<string | null>(null)
const swipeRefs = ref<(SwipeCellInstance | null)[]>([])

function setSwipeRef(el: unknown, index: number) {
  swipeRefs.value[index] = (el as SwipeCellInstance) || null
}

// 手风琴：划开一项时收起其余项
function onOpen(key: string, index: number) {
  activeKey.value = key
  swipeRefs.value.forEach((cell, i) => {
    if (i !== index) cell?.close('right')
  })
}

function onClose(key: string) {
  if (activeKey.value === key) activeKey.value = null
}

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
  margin: 12px 16px;
}

/* 卡片化：圆角 + 阴影 + 裁掉划出按钮的直角，与首页卡片语言一致 */
.swipe-item {
  display: block;
  margin-bottom: 12px;
  border-radius: $radius-lg;
  overflow: hidden;
  background: $color-bg-card-solid;
  box-shadow: $shadow-sm;
  transition: box-shadow 0.28s ease;

  &.is-active {
    box-shadow: $shadow-md;
  }
}

:deep(.van-cell) {
  padding: 14px 16px;
  background: $color-bg-card-solid;
  transition: transform 0.28s cubic-bezier(0.18, 0.89, 0.32, 1), background 0.28s ease;
}

/* 卡片联动：划开时内容轻微内收，明确「当前作用于这张卡」 */
.swipe-item.is-active :deep(.van-cell) {
  transform: scale(0.98);
}

.patient-icon {
  margin-right: 8px;
  color: $color-text-tertiary;
}

.patient-tag {
  margin-right: 8px;
}

.actions {
  display: flex;
  height: 100%;

  .action {
    display: flex;
    width: 64px;
    align-items: center;
    justify-content: center;
    padding: 0;
    border: none;
    outline: none;
    color: $color-text-white;
    font-size: $font-size-sm;
    font-family: inherit;
    cursor: pointer;
    -webkit-tap-highlight-color: transparent;
    background: none;

    &--switch {
      background: $color-text-secondary;
    }

    &--default {
      background: $color-primary;
    }

    &--delete {
      background: $color-danger;
    }

    &:active {
      filter: brightness(0.92);
    }
  }
}

/* 按钮依次滑入淡入；拖动过程中保持可见，仅在吸附展开时做一次收势动画 */
.swipe-item.is-active .actions .action {
  animation: action-settle 0.28s cubic-bezier(0.18, 0.89, 0.32, 1) backwards;

  &:nth-child(2) {
    animation-delay: 40ms;
  }

  &:nth-child(3) {
    animation-delay: 80ms;
  }
}

@keyframes action-settle {
  from {
    opacity: 0.5;
    transform: translateX(10px);
  }

  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.add-btn {
  position: fixed;
  bottom: 24px;
  left: 16px;
  right: 16px;
  width: auto;
}
</style>
