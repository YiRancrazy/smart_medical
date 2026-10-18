<template>
  <page-container title="账户管理">
    <glass-card>
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="用户名">
          <a-input v-model:value="searchForm.username" placeholder="请输入用户名" allow-clear />
        </a-form-item>
        <a-form-item label="角色">
          <a-select
            v-model:value="searchForm.roleId"
            placeholder="请选择角色"
            allow-clear
            style="width: 200px"
          >
            <a-select-option :value="1">管理员</a-select-option>
            <a-select-option :value="2">医生</a-select-option>
            <a-select-option :value="6">药师</a-select-option>
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
          <template v-if="column.key === 'roleId'">
            <a-tag :color="getRoleColor(record.roleId)">
              {{ record.role || getRoleName(record.roleId) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'enabled'">
            <a-tag :color="record.enabled ? 'green' : 'red'">
              {{ record.enabled ? '启用' : '禁用' }}
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

    <!-- 编辑弹窗 -->
    <a-modal v-model:open="editVisible" title="编辑账户" @ok="submitEdit" :confirm-loading="editSubmitting">
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="角色">
          <a-select v-model:value="editForm.roleId" placeholder="请选择角色">
            <a-select-option :value="1">管理员</a-select-option>
            <a-select-option :value="2">医生</a-select-option>
            <a-select-option :value="6">药师</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="启用状态">
          <a-switch v-model:checked="editForm.enabled" checked-children="启用" un-checked-children="禁用" />
        </a-form-item>
        <a-form-item label="手机号">
          <a-input v-model:value="editForm.phone" placeholder="请输入手机号" />
        </a-form-item>
      </a-form>
    </a-modal>
  </page-container>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import { listAccountsByConditions, updateAccount, deleteAccount } from '@/api/admin/account'
import type { AccountDetailResponse } from '@/api/admin/account'
import { message } from 'ant-design-vue'
import Modal from 'ant-design-vue/es/modal'

/**
 * 账户管理页面
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员账户的分页查询、搜索
 * @Datetime: 2026-07-18 18:10
 * @Version: 1.1
 */

const searchForm = ref({
  username: '',
  roleId: undefined as number | undefined
})

const tableData = ref<AccountDetailResponse[]>([])
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
  { title: '用户名', dataIndex: 'username', key: 'username' },
  { title: '手机号', dataIndex: 'phone', key: 'phone' },
  { title: '角色', dataIndex: 'roleId', key: 'roleId', width: 100 },
  { title: '状态', dataIndex: 'enabled', key: 'enabled', width: 80 },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await listAccountsByConditions({
      username: searchForm.value.username || undefined,
      roleId: searchForm.value.roleId,
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize
    })
    if (res.data) {
      tableData.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    message.error('加载账户列表失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.value.current = 1
  loadData()
}

function handleReset() {
  searchForm.value = {
    username: '',
    roleId: undefined
  }
  pagination.value.current = 1
  loadData()
}

function handleTableChange(pag: any) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

function getRoleName(roleId: any) {
  const map: Record<string, string> = { '1': '管理员', '2': '医生', '6': '药师' }
  return map[String(roleId)] || '未知'
}

function getRoleColor(roleId: any) {
  const map: Record<string, string> = { '1': 'blue', '2': 'green', '6': 'orange' }
  return map[String(roleId)] || 'default'
}

// F20: 编辑弹窗状态
const editVisible = ref(false)
const editSubmitting = ref(false)
const editingId = ref<string | number>('')
const editForm = reactive({
  roleId: undefined as number | undefined,
  enabled: true,
  phone: ''
})

function handleEdit(record: AccountDetailResponse) {
  editingId.value = record.id
  editForm.roleId = Number(record.roleId) || undefined
  editForm.enabled = !!record.enabled
  editForm.phone = record.phone || ''
  editVisible.value = true
}

async function submitEdit() {
  if (!editingId.value) return
  editSubmitting.value = true
  try {
    await updateAccount(editingId.value, {
      roleId: editForm.roleId,
      enabled: editForm.enabled,
      phone: editForm.phone
    })
    message.success('更新成功')
    editVisible.value = false
    loadData()
  } catch {
    // 拦截器已弹窗
  } finally {
    editSubmitting.value = false
  }
}

function handleDelete(record: AccountDetailResponse) {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除账户 ${record.username || record.id} 吗？`,
    okType: 'danger',
    onOk: async () => {
      try {
        await deleteAccount(record.id)
        message.success('删除成功')
        loadData()
      } catch {
        // 拦截器已弹窗
      }
    }
  })
}
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}

.data-table {
  margin-top: 16px;
}
</style>
