<template>
  <a-tree-select
    v-model:value="selectedId"
    :tree-data="treeData"
    :field-names="{ label: 'name', value: 'id', children: 'children' }"
    placeholder="请选择科室"
    allow-clear
    tree-default-expand-all
    @change="handleChange"
  />
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { listDepartments } from '@/api/admin/department'
import type { DepartmentSimple } from '@/api/admin/department'
import { message } from 'ant-design-vue'

/**
 * 科室树选择器
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支持一级/二级科室树形选择
 * @Datetime: 2026-07-17 11:25
 * @Version: 1.0
 */

// F03: 调用方使用 v-model:value，要求 prop=value + emit=update:value（原 modelValue 契约失效）
// F02/F20: 雪花 ID 超 2^53，统一用 string，避免 Number() 截断导致 Map 覆盖丢节点
const props = defineProps<{
  value?: string | number
}>()

const emit = defineEmits<{
  (e: 'update:value', value: string | undefined): void
  (e: 'change', value: string | undefined, node: any): void
}>()

const selectedId = ref<string | undefined>(props.value ? String(props.value) : undefined)
const treeData = ref<any[]>([])

// F03: 父→子同步缺失，仅初始化一次；补 watch
watch(() => props.value, (v) => {
  selectedId.value = v == null || v === '' ? undefined : String(v)
})

onMounted(async () => {
  await loadTreeData()
})

/**
 * 后端 /list/tree 序列化 Map 异常，改用 /list 自己构建树
 */
async function loadTreeData() {
  try {
    const res = await listDepartments()
    const all = (res.data || []) as DepartmentSimple[]
    treeData.value = buildTree(all)
  } catch (error) {
    message.error('加载科室树失败')
  }
}

function buildTree(list: any[]): any[] {
  // F20: 用 string id 作 Map key，避免 Number(雪花ID) 截断后不同 ID 落到同一 bucket
  const map = new Map<string, any>()
  list.forEach((d) => {
    const id = String(d.id)
    map.set(id, { ...d, id, children: [] as any[] })
  })
  const roots: any[] = []
  map.forEach((node) => {
    // 后端字段是 parentDepartmentId，且无父级时为字符串 "null"
    const raw = node.parentDepartmentId ?? node.parentId
    const pid = raw == null || raw === '' || String(raw) === 'null' ? '' : String(raw)
    if (pid && map.has(pid)) {
      map.get(pid).children.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

function handleChange(value: any, _label: any, extra: any) {
  const v = value == null ? undefined : String(value)
  emit('update:value', v)
  emit('change', v, extra)
}

defineExpose({
  reload: loadTreeData
})
</script>