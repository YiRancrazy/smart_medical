<template>
  <page-container title="排班管理">
    <glass-card>
      <!-- 搜索表单 -->
      <a-form layout="inline" :model="searchForm" class="search-form">
        <a-form-item label="医生">
          <DoctorSelect
            v-model:value="searchForm.doctorId"
            placeholder="请选择医生"
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="科室">
          <DepartmentTree
            v-model:value="searchForm.departmentId"
            placeholder="请选择科室"
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="排班日期">
          <a-range-picker v-model:value="searchForm.dateRange" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">搜索</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <!-- Excel 导入区域 -->
      <div class="upload-section">
        <div class="upload-row">
          <a-button type="primary" :loading="uploading" @click="triggerFileInput">
            <upload-outlined />
            导入排班模板
          </a-button>
          <input
            ref="fileInputRef"
            type="file"
            accept=".csv,.xls,.xlsx"
            style="display: none"
            @change="handleFileChange"
          />
          <a-button @click="handleDownloadTemplate" class="download-btn">
            <download-outlined />
            下载导入模板
          </a-button>
        </div>
        <span class="upload-hint">支持 .csv / .xls / .xlsx，按模板格式填写后导入</span>
      </div>

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
          <template v-if="column.key === 'status'">
            <a-tag :color="getScheduleStatus(record.status).color">
              {{ getScheduleStatus(record.status).label }}
            </a-tag>
          </template>
          <template v-if="column.key === 'quota'">
            <span>{{ record.remaining ?? '-' }} / {{ record.total ?? '-' }}</span>
          </template>
          <template v-if="column.key === 'scheduleType'">
            <span>{{ getShiftName(record.scheduleType) }}</span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button
                v-if="isScheduleActive(record.status)"
                type="link"
                size="small"
                danger
                @click="handleStop(record)"
              >
                停诊
              </a-button>
              <a-button
                v-else
                type="link"
                size="small"
                @click="handleStart(record)"
              >
                启用
              </a-button>
              <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </glass-card>
  </page-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import DepartmentTree from '@/components/business/DepartmentTree.vue'
import DoctorSelect from '@/components/business/DoctorSelect.vue'
import { UploadOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import {
  listScheduleTemplates,
  stopSchedule,
  startSchedule,
  deleteSchedule
} from '@/api/admin/schedule'
import type { ScheduleTemplateResponse } from '@/api/admin/schedule'
import { uploadRegistrationTemplate, downloadRegistrationTemplate } from '@/api/admin/excel'
import { message } from 'ant-design-vue'
import Modal from 'ant-design-vue/es/modal'

/**
 * 排班管理页面
 * @Author: YiRanCrazy@gmail.com
 * @Description: 排班的查询、停诊、启用、删除 + Excel 导入
 * @Datetime: 2026-07-18 17:30
 * @Version: 1.0
 */

const searchForm = ref({
  doctorId: undefined as number | undefined,
  departmentId: undefined as number | undefined,
  dateRange: undefined as [string, string] | undefined
})

const tableData = ref<ScheduleTemplateResponse[]>([])
const loading = ref(false)
const uploading = ref(false)

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
  { title: '医生', dataIndex: 'doctorName', key: 'doctorName' },
  { title: '科室', dataIndex: 'departmentName', key: 'departmentName' },
  { title: '班次', dataIndex: 'scheduleType', key: 'scheduleType' },
  { title: '排班日期', dataIndex: 'scheduleDate', key: 'scheduleDate' },
  { title: '时段', key: 'time', customRender: ({ record }: any) => `${record.startTime ?? '-'} - ${record.endTime ?? '-'}` },
  { title: '剩余/总数', key: 'quota', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
  // F30: price 为空时整体显示"-"，后端返回分为单位，展示时除以 100
  { title: '价格', key: 'price', width: 100, customRender: ({ record }: any) => record.price != null ? `¥${(record.price / 100).toFixed(2)}` : '-' },
  { title: '操作', key: 'action', width: 150, fixed: 'right' }
]

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await listScheduleTemplates({
      pageNum: pagination.value.current,
      pageSize: pagination.value.pageSize,
      doctorId: searchForm.value.doctorId,
      departmentId: searchForm.value.departmentId,
      startDate: searchForm.value.dateRange?.[0],
      endDate: searchForm.value.dateRange?.[1]
    })
    if (res.data) {
      tableData.value = res.data.list || []
      pagination.value.total = res.data.total || 0
    }
  } catch (error) {
    message.error('加载排班列表失败')
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
    doctorId: undefined,
    departmentId: undefined,
    dateRange: undefined
  }
  pagination.value.current = 1
  loadData()
}

function handleTableChange(pag: any) {
  pagination.value.current = pag.current
  pagination.value.pageSize = pag.pageSize
  loadData()
}

const fileInputRef = ref<HTMLInputElement | null>(null)

/** 触发文件选择 */
function triggerFileInput() {
  fileInputRef.value?.click()
}

/** 文件选择后上传 */
async function handleFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0]
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadRegistrationTemplate(file)
    if (res.code === 200) {
      message.success(`导入成功，新增 ${res.data ?? 0} 条排班`)
      // F33: 重置到第一页，否则当前页超出新数据范围看不到
      pagination.value.current = 1
      loadData()
    } else {
      message.error(res.message || '导入失败')
    }
  } catch {
    message.error('导入失败，请检查文件格式')
  } finally {
    uploading.value = false
    // 重置 input 以便再次选择同一文件
    if (fileInputRef.value) fileInputRef.value.value = ''
  }
}

/** 下载导入模板 */
async function handleDownloadTemplate() {
  try {
    const blob = await downloadRegistrationTemplate()
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '挂号排班导入模板.csv'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
  } catch {
    message.error('下载模板失败')
  }
}

async function handleStop(record: ScheduleTemplateResponse) {
  Modal.confirm({
    title: '确认停诊',
    content: `确定要停诊"${record.doctorName}"的排班吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await stopSchedule(record.id)
        message.success('停诊成功')
        loadData()
      } catch {
        // M20: 业务错误已在拦截器统一提示，避免二次 toast
      }
    }
  })
}

async function handleStart(record: ScheduleTemplateResponse) {
  try {
    await startSchedule(record.id)
    message.success('启用成功')
    loadData()
  } catch (error) {
    message.error('启用失败')
  }
}

function handleDelete(record: ScheduleTemplateResponse) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除该排班模板吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      try {
        await deleteSchedule(record.id)
        message.success('删除成功')
        loadData()
      } catch (error) {
        message.error('删除失败')
      }
    }
  })
}

/**
 * 排班状态映射（兼容后端 0/1 数字、"true"/"false" 字符串）
 */
function getScheduleStatus(s: any): { label: string; color: string; active: boolean } {
  const v = String(s).toLowerCase()
  const active = v === '1' || v === 'true'
  return active
    ? { label: '启用', color: 'green', active: true }
    : { label: '停诊', color: 'default', active: false }
}
function isScheduleActive(s: any) {
  return getScheduleStatus(s).active
}

/**
 * 班次名称（根据 scheduleType 推断）
 */
const SHIFT_NAME: Record<string, string> = {
  '0': '上午',
  '1': '下午',
  '2': '夜间'
}
function getShiftName(type: any) {
  return SHIFT_NAME[String(type)] || '-'
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.search-form {
  margin-bottom: 16px;
}

.upload-section {
  margin-bottom: 16px;
  padding: @spacing-md @spacing-base;
  background: @glass-bg-subtle;
  border-radius: @border-radius-base;
  border: 1px dashed @border-color;
}

.upload-row {
  display: flex;
  align-items: center;
  gap: @spacing-sm;
}

.download-btn {
  margin-left: @spacing-sm;
}

.upload-hint {
  display: inline-block;
  margin-top: @spacing-xs;
  font-size: @font-size-xs;
  color: @text-color-secondary;
}

.data-table {
  margin-top: 16px;
}
</style>
