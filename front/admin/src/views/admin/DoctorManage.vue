<template>
  <page-container title="医生管理">
    <glass-card>
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="医生姓名">
          <a-input v-model:value="searchForm.username" placeholder="请输入医生姓名" allow-clear />
        </a-form-item>
        <a-form-item label="所属科室">
          <DepartmentTree
            v-model:value="searchForm.departmentId"
            placeholder="请选择科室"
            style="width: 200px"
          />
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
        row-key="doctorId"
        class="data-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="doctorStatusColor(record.status)">
              {{ doctorStatusText(record.status) }}
            </a-tag>
          </template>
          <template v-if="column.key === 'tags'">
            <a-tag v-for="tag in parseTags(record.tags)" :key="tag" color="blue">
              {{ tag }}
            </a-tag>
            <span v-if="!record.tags || parseTags(record.tags).length === 0">-</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
              <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </glass-card>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      title="医生详情"
      width="800px"
      :footer="null"
    >
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="医生姓名">{{ currentDoctor?.doctorName }}</a-descriptions-item>
        <a-descriptions-item label="所属科室">{{ currentDoctor?.departmentName }}</a-descriptions-item>
        <a-descriptions-item label="职称">{{ currentDoctor?.positionName }}</a-descriptions-item>
        <a-descriptions-item label="学历">{{ currentDoctor?.degreeName }}</a-descriptions-item>
        <a-descriptions-item label="地址">{{ currentDoctor?.address || '-' }}</a-descriptions-item>
        <a-descriptions-item label="评分">{{ currentDoctor?.scope || '-' }}</a-descriptions-item>
        <a-descriptions-item label="标签" :span="2">
          <a-tag v-for="tag in parseTags(currentDoctor?.tags)" :key="tag" color="blue">
            {{ tag }}
          </a-tag>
          <span v-if="!currentDoctor?.tags || parseTags(currentDoctor?.tags).length === 0">-</span>
        </a-descriptions-item>
        <a-descriptions-item label="简介" :span="2">{{ currentDoctor?.description || '-' }}</a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="doctorStatusColor(currentDoctor?.status)">
            {{ doctorStatusText(currentDoctor?.status) }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>

    <!-- 编辑弹窗 -->
    <a-modal
      v-model:open="editVisible"
      title="编辑医生"
      @ok="handleEditOk"
      @cancel="editVisible = false"
    >
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="医生姓名" required>
          <a-input v-model:value="editForm.doctorName" />
        </a-form-item>
        <a-form-item label="简介">
          <a-textarea v-model:value="editForm.description" :rows="3" />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="editForm.status" style="width: 120px">
            <a-select-option value="0">在职</a-select-option>
            <a-select-option value="1">休假</a-select-option>
            <a-select-option value="2">出差</a-select-option>
            <a-select-option value="3">离职</a-select-option>
          </a-select>
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
import { listDoctorsDetail, deleteDoctor, updateDoctor } from '@/api/admin/doctor'
import type { DoctorDetailResponse } from '@/api/admin/doctor'
import { message } from 'ant-design-vue'
import Modal from 'ant-design-vue/es/modal'

/**
 * 医生管理页面
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生列表查看、搜索、编辑、删除
 * @Datetime: 2026-07-21 20:00
 * @Version: 1.2
 */

const searchForm = ref({
  username: '',
  departmentId: undefined as number | undefined
})

const tableData = ref<DoctorDetailResponse[]>([])
const loading = ref(false)
const detailVisible = ref(false)
const currentDoctor = ref<DoctorDetailResponse | null>(null)

const editVisible = ref(false)
const editForm = ref({
  doctorId: '',
  doctorName: '',
  description: '',
  status: '0'
})

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`
})

const columns = [
  { title: 'ID', dataIndex: 'doctorId', key: 'doctorId', width: 80 },
  { title: '医生姓名', dataIndex: 'doctorName', key: 'doctorName' },
  { title: '所属科室', dataIndex: 'departmentName', key: 'departmentName' },
  { title: '职称', dataIndex: 'positionName', key: 'positionName' },
  { title: '学历', dataIndex: 'degreeName', key: 'degreeName' },
  { title: '标签', dataIndex: 'tags', key: 'tags', width: 150 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  { title: '操作', key: 'action', width: 200, fixed: 'right' }
]

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await listDoctorsDetail({
      username: searchForm.value.username || undefined,
      departmentId: searchForm.value.departmentId,
      current: pagination.value.current,
      size: pagination.value.pageSize
    })
    if (res.data) {
      tableData.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    message.error('加载医生列表失败')
  } finally {
    loading.value = false
  }
}

/** 安全解析 tags：兼容字符串、数组、null */
function parseTags(tags: any): string[] {
  if (!tags) return []
  if (Array.isArray(tags)) return tags.filter(Boolean)
  if (typeof tags === 'string') return tags.split(',').filter(Boolean)
  return [String(tags)]
}

/** 医生状态文案：0=在职 1=休假 2=出差 3=离职 */
function doctorStatusText(status: any): string {
  const map: Record<string, string> = { '0': '在职', '1': '休假', '2': '出差', '3': '离职' }
  return map[String(status)] ?? '未知'
}

/** 医生状态颜色 */
function doctorStatusColor(status: any): string {
  const map: Record<string, string> = { '0': 'green', '1': 'orange', '2': 'blue', '3': 'red' }
  return map[String(status)] ?? 'default'
}

function handleSearch() {
  pagination.value.current = 1
  loadData()
}

function handleReset() {
  searchForm.value = {
    username: '',
    departmentId: undefined
  }
  pagination.value.current = 1
  loadData()
}

function handleTableChange(pag: any) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

function handleView(record: DoctorDetailResponse) {
  currentDoctor.value = record
  detailVisible.value = true
}

function handleEdit(record: DoctorDetailResponse) {
  editForm.value = {
    doctorId: record.doctorId,
    doctorName: record.doctorName,
    description: record.description || '',
    // F27: 用 || 兜底空串，?? 仅对 null/undefined 生效，空串会残留导致下拉空白
    status: record.status || '0'
  }
  editVisible.value = true
}

async function handleEditOk() {
  if (!editForm.value.doctorName) {
    message.warning('请填写医生姓名')
    return
  }
  try {
    await updateDoctor(editForm.value.doctorId, {
      doctorName: editForm.value.doctorName,
      description: editForm.value.description,
      status: editForm.value.status
    })
    message.success('修改成功')
    editVisible.value = false
    loadData()
  } catch {
    // M20: 业务错误已在拦截器统一提示，避免二次 toast
  }
}

function handleDelete(record: DoctorDetailResponse) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除医生"${record.doctorName}"吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteDoctor(record.doctorId)
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
.search-form {
  margin-bottom: 16px;
}

.data-table {
  margin-top: 16px;
}
</style>
