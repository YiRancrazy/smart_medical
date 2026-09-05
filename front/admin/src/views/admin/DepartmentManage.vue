<template>
  <page-container title="科室管理">
    <glass-card>
      <!-- 操作栏 -->
      <a-space class="action-bar">
        <a-button type="primary" @click="handleAdd">新增科室</a-button>
        <a-radio-group v-model:value="deptLevel" @change="handleLevelChange">
          <a-radio-button value="parent">一级科室</a-radio-button>
          <a-radio-button value="child">二级科室</a-radio-button>
        </a-radio-group>
      </a-space>

      <!-- 搜索表单 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="科室名称">
          <a-input v-model:value="searchForm.name" placeholder="请输入科室名称" allow-clear />
        </a-form-item>
        <a-form-item label="科室编号">
          <a-input v-model:value="searchForm.sn" placeholder="请输入科室编号" allow-clear />
        </a-form-item>
        <a-form-item v-if="deptLevel === 'child'" label="所属科室">
          <DepartmentTree v-model:value="searchForm.parentId" placeholder="请选择上级科室" style="width: 200px" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="searchForm.status" placeholder="请选择状态" allow-clear style="width: 120px">
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- 数据表格 -->
      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="pagination"
        @change="handleTableChange"
        row-key="id"
        class="data-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'type'">
            <a-tag :color="getTypeColor(record.type)">
              {{ getTypeName(record.type) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'status'">
            <a-tag :color="Number(record.status) === 1 ? 'green' : 'default'">
              {{ Number(record.status) === 1 ? '启用' : '停用' }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </glass-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-form :model="formData" layout="vertical">
        <a-form-item label="科室名称" required>
          <a-input v-model:value="formData.name" placeholder="请输入科室名称" />
        </a-form-item>
        <a-form-item label="科室编号" required>
          <a-input v-model:value="formData.sn" placeholder="请输入科室编号" />
        </a-form-item>
        <a-form-item v-if="deptLevel === 'child'" label="上级科室" required>
          <DepartmentTree v-model:value="formData.parentId" placeholder="请选择上级科室" />
        </a-form-item>
        <a-form-item label="科室类型" required>
          <a-radio-group v-model:value="formData.type">
            <a-radio value="0">临床科室</a-radio>
            <a-radio value="1">医技科室</a-radio>
            <a-radio value="2">行政科室</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="状态">
          <a-radio-group v-model:value="formData.status">
            <a-radio value="1">启用</a-radio>
            <a-radio value="0">停用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </page-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import DepartmentTree from '@/components/business/DepartmentTree.vue'
import {
  listDepartmentsByConditions,
  listParentDepartments,
  listChildDepartments,
  createDepartment,
  updateDepartment,
  deleteDepartment
} from '@/api/admin/department'
import type { DepartmentSimple } from '@/api/admin/department'
import { message } from 'ant-design-vue'
import Modal from 'ant-design-vue/es/modal'

/**
 * 科室管理页面
 * @Author: YiRanCrazy@gmail.com
 * @Description: 一级/二级科室的增删改查
 * @Datetime: 2026-07-17 12:10
 * @Version: 1.0
 */

const deptLevel = ref<'parent' | 'child'>('parent')
const searchForm = ref({
  name: '',
  sn: '',
  parentId: undefined as number | undefined,
  status: undefined as number | undefined
})

const tableData = ref<DepartmentSimple[]>([])
const loading = ref(false)

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
  { title: '科室名称', dataIndex: 'name', key: 'name' },
  { title: '科室编号', dataIndex: 'sn', key: 'sn' },
  { title: '科室类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '上级科室', dataIndex: 'parentDepartmentName', key: 'parentDepartmentName', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

const modalVisible = ref(false)
const modalTitle = ref('新增科室')
const formData = ref({
  id: undefined as string | undefined,
  name: '',
  sn: '',
  parentId: undefined as string | undefined,
  type: '0',
  status: '1'
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const pageNum = pagination.value.current
    const pageSize = pagination.value.pageSize
    // F14: 二级科室模式下选了上级科室也应走 conditions 接口
    const hasSearch = !!searchForm.value.name || !!searchForm.value.sn || searchForm.value.status !== undefined
      || (deptLevel.value === 'child' && !!searchForm.value.parentId)
    let res: any
    if (hasSearch) {
      // 搜索走 conditions 接口，支持 name/sn/status 过滤
      res = await listDepartmentsByConditions({
        name: searchForm.value.name || undefined,
        sn: searchForm.value.sn || undefined,
        parentId: deptLevel.value === 'child' ? searchForm.value.parentId : undefined,
        status: searchForm.value.status,
        current: pageNum,
        size: pageSize
      })
    } else if (deptLevel.value === 'parent') {
      res = await listParentDepartments(pageNum, pageSize)
    } else {
      res = await listChildDepartments(pageNum, pageSize)
    }
    if (res.data) {
      tableData.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    message.error('加载科室列表失败')
  } finally {
    loading.value = false
  }
}

function handleLevelChange() {
  pagination.value.current = 1
  loadData()
}

function handleSearch() {
  pagination.value.current = 1
  loadData()
}

function handleReset() {
  searchForm.value = {
    name: '',
    sn: '',
    parentId: undefined,
    status: undefined
  }
  pagination.value.current = 1
  loadData()
}

function handleTableChange(pag: any) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

function handleAdd() {
  modalTitle.value = '新增科室'
  formData.value = {
    id: undefined,
    name: '',
    sn: '',
    parentId: undefined,
    type: '0',
    status: '1'
  }
  modalVisible.value = true
}

function handleEdit(record: DepartmentSimple) {
  modalTitle.value = '编辑科室'
  formData.value = {
    id: record.id,
    name: record.name,
    sn: record.sn || '',
    parentId: record.parentDepartmentId || undefined,
    type: record.type || '0',
    status: record.status || '1'
  }
  modalVisible.value = true
}

async function handleModalOk() {
  if (!formData.value.name || !formData.value.sn) {
    message.warning('请填写必填项')
    return
  }
  // F13: 二级科室必须选上级科室，避免无父级的"二级科室"破坏科室树
  if (deptLevel.value === 'child' && !formData.value.parentId) {
    message.warning('请选择上级科室')
    return
  }

  try {
    if (formData.value.id) {
      await updateDepartment(formData.value.id, formData.value)
      message.success('修改成功')
    } else {
      // F28: 新增时剔除 undefined 的 id，避免污染请求体
      const { id, ...createPayload } = formData.value
      await createDepartment(createPayload)
      message.success('新增成功')
    }
    modalVisible.value = false
    loadData()
  } catch {
    // M20: 业务错误已在拦截器统一提示，避免二次 toast
  }
}

function handleModalCancel() {
  modalVisible.value = false
}

/**
 * 科室类型映射（后端 0=临床 1=医技 2=行政）
 */
const DEPT_TYPE_NAME: Record<number, string> = {
  0: '临床科室',
  1: '医技科室',
  2: '行政科室'
}
const DEPT_TYPE_COLOR: Record<number, string> = {
  0: 'blue',
  1: 'green',
  2: 'orange'
}
function getTypeName(t: number | string) {
  return DEPT_TYPE_NAME[Number(t)] || '其他'
}
function getTypeColor(t: number | string) {
  return DEPT_TYPE_COLOR[Number(t)] || 'default'
}

function handleDelete(record: DepartmentSimple) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除科室"${record.name}"吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteDepartment(record.id)
        message.success('删除成功')
        loadData()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}
</script>

<style scoped>
.action-bar {
  margin-bottom: 16px;
}

.search-form {
  margin-bottom: 16px;
}

.data-table {
  margin-top: 16px;
}
</style>