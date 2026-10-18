<template>
  <page-container title="工作台">
    <a-spin :spinning="loading">
      <a-row :gutter="16">
        <a-col v-for="item in stats" :key="item.label" :span="6">
          <glass-card class="stat-card">
            <div class="stat-icon" :style="{ background: item.bg }">
              <component :is="item.icon" />
            </div>
            <div class="stat-meta">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-label">{{ item.label }}</div>
            </div>
          </glass-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" class="section-row">
        <a-col :span="16">
          <glass-card title="快捷入口">
            <a-row :gutter="16">
              <a-col v-for="entry in quickEntries" :key="entry.label" :span="8">
                <div class="quick-entry" @click="$router.push({ name: entry.name })">
                  <div class="quick-icon" :style="{ background: entry.bg }">
                    <component :is="entry.icon" />
                  </div>
                  <div class="quick-label">{{ entry.label }}</div>
                </div>
              </a-col>
            </a-row>
          </glass-card>
        </a-col>
        <a-col :span="8">
          <glass-card title="待办提醒">
            <div class="todo-list">
              <div v-for="todo in todos" :key="todo.label" class="todo-item">
                <span class="todo-label">{{ todo.label }}</span>
                <span class="todo-count">{{ todo.count }}</span>
              </div>
            </div>
          </glass-card>
        </a-col>
      </a-row>
    </a-spin>
  </page-container>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import {
  FileTextOutlined,
  ClockCircleOutlined,
  MedicineBoxOutlined,
  ExperimentOutlined,
  UserOutlined,
  CalendarOutlined,
  MedicineBoxTwoTone,
  ReconciliationOutlined
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { getDashboardStats, type DashboardStats } from '@/api/admin/dashboard'

/**
 * 管理员工作台
 * @Author: YiRanCrazy@gmail.com
 * @Description: 展示今日统计数据、快捷入口和待办提醒
 * @Datetime: 2026-07-17 11:45
 * @Version: 1.0
 */

const loading = ref(false)
const stats = ref([
  { label: '今日挂号', value: 0, icon: FileTextOutlined, bg: 'rgba(16, 185, 129, 0.12)' },
  { label: '待就诊', value: 0, icon: ClockCircleOutlined, bg: 'rgba(245, 158, 11, 0.12)' },
  { label: '就诊中', value: 0, icon: MedicineBoxOutlined, bg: 'rgba(52, 199, 89, 0.12)' },
  { label: '待发药', value: 0, icon: ExperimentOutlined, bg: 'rgba(90, 200, 250, 0.12)' }
])

const quickEntries = ref([
  // L5: 用路由 name 导航而非硬编码 path，避免与动态路由路径脱钩
  { label: '医生管理', name: 'DoctorManage', path: '/admin/doctors', icon: UserOutlined, bg: 'rgba(16, 185, 129, 0.12)' },
  { label: '排班模板', name: 'ScheduleTemplate', path: '/admin/schedule-templates', icon: CalendarOutlined, bg: 'rgba(90, 200, 250, 0.12)' },
  { label: '科室管理', name: 'DepartmentManage', path: '/admin/departments', icon: MedicineBoxTwoTone, bg: 'rgba(245, 158, 11, 0.12)' },
  { label: '账户管理', name: 'AccountManage', path: '/admin/accounts', icon: ReconciliationOutlined, bg: 'rgba(139, 92, 246, 0.12)' }
])

const todos = ref([
  { label: '待审核退号', count: 0 },
  { label: '库存预警', count: 0 },
  { label: '新处方待处理', count: 0 }
])

onMounted(async () => {
  await loadStats()
})

async function loadStats() {
  loading.value = true
  try {
    const res = await getDashboardStats()
    const data: DashboardStats = res.data
    stats.value[0].value = data.todayRegistrationCount
    stats.value[1].value = data.waitingVisitCount
    stats.value[2].value = data.inTreatmentCount
    stats.value[3].value = data.pendingDispenseCount
    todos.value[0].count = data.pendingRefundCount
    todos.value[1].count = data.inventoryAlertCount
    todos.value[2].count = data.newPrescriptionCount
  } catch (error) {
    message.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.stat-card {
  display: flex;
  align-items: center;
  gap: @spacing-base;
}

.stat-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: @border-radius-base;
  color: @primary-color;
  font-size: 24px;
}

.stat-value {
  font-size: 28px;
  font-weight: @font-weight-semibold;
  color: @text-color;
  line-height: 1.2;
}

.stat-label {
  color: @text-color-secondary;
  font-size: @font-size-sm;
  margin-top: 2px;
}

.section-row {
  margin-top: @spacing-lg;
}

.quick-entry {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: @spacing-sm;
  padding: @spacing-base;
  border-radius: @border-radius-base;
  cursor: pointer;
  transition: background 0.2s;

  &:hover {
    background: rgba(16, 185, 129, 0.06);
  }
}

.quick-icon {
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: @border-radius-lg;
  color: @primary-color;
  font-size: 22px;
}

.quick-label {
  font-size: @font-size-sm;
  color: @text-color-secondary;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: @spacing-base;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: @spacing-sm @spacing-base;
  background: @glass-bg;
  border-radius: @border-radius-base;
}

.todo-label {
  color: @text-color-secondary;
  font-size: @font-size-sm;
}

.todo-count {
  min-width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 6px;
  border-radius: 11px;
  background: @primary-color-bg;
  color: @primary-color;
  font-size: @font-size-xs;
  font-weight: @font-weight-medium;
}
</style>
