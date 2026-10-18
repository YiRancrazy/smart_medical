<template>
  <a-select
    v-model:value="selectedId"
    show-search
    placeholder="请选择医生"
    :filter-option="false"
    :not-found-content="loading ? undefined : '暂无数据'"
    @search="handleSearch"
    @change="handleChange"
  >
    <template v-if="loading" #notFoundContent>
      <a-spin size="small" />
    </template>
    <a-select-option v-for="item in doctors" :key="item.doctorId" :value="item.doctorId">
      {{ item.doctorName }} - {{ item.departmentName }}
    </a-select-option>
  </a-select>
</template>

<script setup lang="ts">
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { listDoctorsSimple } from '@/api/admin/doctor'
import type { DoctorSimpleResponse } from '@/api/admin/doctor'
import { message } from 'ant-design-vue'

/**
 * 医生选择器
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支持姓名模糊搜索的医生选择
 * @Datetime: 2026-07-17 11:30
 * @Version: 1.0
 */

// F04: 调用方使用 v-model:value，要求 prop=value + emit=update:value
// F02: 雪花 ID 用 string，避免 number 截断
const props = defineProps<{
  value?: string | number
}>()

const emit = defineEmits<{
  (e: 'update:value', value: string | undefined): void
  (e: 'change', value: string | undefined, doctor: DoctorSimpleResponse | undefined): void
}>()

const selectedId = ref<string | undefined>(props.value ? String(props.value) : undefined)
const doctors = ref<DoctorSimpleResponse[]>([])
const loading = ref(false)
const searchTimer = ref<ReturnType<typeof setTimeout>>()

watch(() => props.value, (newVal) => {
  selectedId.value = newVal == null || newVal === '' ? undefined : String(newVal)
})

// 挂载即加载全量医生，保证打开下拉就能看到列表
onMounted(() => {
  searchDoctors('')
})

// F23: 卸载时清理定时器，避免更新已卸载组件
onUnmounted(() => {
  if (searchTimer.value) clearTimeout(searchTimer.value)
})

function handleSearch(value: string) {
  if (searchTimer.value) {
    clearTimeout(searchTimer.value)
  }
  searchTimer.value = setTimeout(() => {
    searchDoctors(value)
  }, 300)
}

async function searchDoctors(name: string) {
  loading.value = true
  try {
    // 空关键词返回全量医生（后端 name 为空时不加 like 条件）
    const res = await listDoctorsSimple(name.trim())
    doctors.value = res.data || []
  } catch (error) {
    message.error('查询医生失败')
  } finally {
    loading.value = false
  }
}

function handleChange(value: any) {
  const v = value == null ? undefined : String(value)
  emit('update:value', v)
  const doctor = doctors.value.find(d => String(d.doctorId) === v)
  emit('change', v, doctor)
}
</script>