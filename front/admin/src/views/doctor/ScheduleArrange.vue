<template>
  <page-container title="排班安排" sub-title="查看个人出诊排班（不含患者信息）">
    <template #extra>
      <a-space>
        <a-button @click="prev"><left-outlined /></a-button>
        <a-tag class="current-date">{{ selectedDate }}</a-tag>
        <a-button @click="next"><right-outlined /></a-button>
        <a-button @click="goToday">今天</a-button>
      </a-space>
    </template>

    <glass-card>
      <a-radio-group v-model:value="viewMode" button-style="solid" class="view-switch">
        <a-radio-button value="day">日视图</a-radio-button>
        <a-radio-button value="week">周视图</a-radio-button>
        <a-radio-button value="month">月视图</a-radio-button>
      </a-radio-group>

      <a-spin :spinning="loading">
        <!-- 日视图：上午 / 下午 / 晚上分组卡片 -->
        <template v-if="viewMode === 'day'">
          <a-empty v-if="!daySchedules.length" description="当日暂无排班" />
          <div v-for="group in dayGroups" :key="group.label" class="day-group">
            <template v-if="group.items.length">
              <div class="day-group__title">{{ group.label }}</div>
              <div class="schedule-grid">
                <div v-for="item in group.items" :key="item.scheduleId" class="schedule-card" :class="{ 'schedule-card--off': item.status === 0 }">
                  <div class="schedule-card__head">
                    <a-tag :color="statusColor(item.status)">{{ statusLabel(item.status) }}</a-tag>
                    <span class="schedule-card__shift">{{ item.shiftName || '-' }}</span>
                  </div>
                  <div class="schedule-card__row"><clock-circle-outlined /> {{ fmtTime(item.startTime) }} - {{ fmtTime(item.endTime) }}</div>
                  <div class="schedule-card__row"><environment-outlined /> {{ item.location || '-' }}</div>
                  <div v-if="item.remark" class="schedule-card__row schedule-card__row--remark"><file-text-outlined /> {{ item.remark }}</div>
                </div>
              </div>
            </template>
          </div>
        </template>

        <!-- 周视图：7 列日历 -->
        <template v-else-if="viewMode === 'week'">
          <div class="week-grid">
            <div v-for="day in weekDays" :key="day.date" class="week-grid__cell" :class="{ 'week-grid__cell--today': day.isToday }">
              <div class="week-grid__date">
                {{ day.weekLabel }}
                <span class="week-grid__date-num">{{ day.date.slice(8) }}</span>
              </div>
              <div v-if="!schedulesOf(day.date).length" class="week-grid__empty">无排班</div>
              <div v-for="item in schedulesOf(day.date)" :key="item.scheduleId" class="schedule-chip" :class="{ 'schedule-chip--off': item.status === 0 }">
                <div class="schedule-chip__time">{{ fmtTime(item.startTime) }} - {{ fmtTime(item.endTime) }}</div>
                <div class="schedule-chip__shift">{{ item.shiftName || '-' }}</div>
                <div class="schedule-chip__location">{{ item.location || '-' }}</div>
                <div v-if="item.status === 0" class="schedule-chip__off-tag">停诊</div>
              </div>
            </div>
          </div>
        </template>

        <!-- 月视图：月历格子 -->
        <template v-else>
          <div class="month-grid">
            <div v-for="label in WEEK_LABELS" :key="label" class="month-grid__head">{{ label }}</div>
            <div
              v-for="cell in monthCells"
              :key="cell.date"
              class="month-grid__cell"
              :class="{
                'month-grid__cell--other': !cell.inMonth,
                'month-grid__cell--today': cell.isToday,
                'month-grid__cell--selected': cell.date === selectedDate
              }"
              @click="cell.inMonth && goDay(cell.date)"
            >
              <div class="month-grid__day">{{ Number(cell.date.slice(8)) }}</div>
              <div v-for="item in schedulesOf(cell.date).slice(0, 2)" :key="item.scheduleId" class="month-dot" :class="{ 'month-dot--off': item.status === 0 }">
                {{ fmtTime(item.startTime) }} {{ item.shiftName || '' }}
              </div>
              <div v-if="schedulesOf(cell.date).length > 2" class="month-dot month-dot--more">等 {{ schedulesOf(cell.date).length }} 项</div>
            </div>
          </div>
        </template>
      </a-spin>
    </glass-card>
  </page-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  ClockCircleOutlined,
  EnvironmentOutlined,
  FileTextOutlined,
  LeftOutlined,
  RightOutlined
} from '@ant-design/icons-vue'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import { getMonthSchedule } from '@/api/doctor/schedule'
import type { DoctorScheduleViewVO } from '@/api/doctor/schedule'
import { message } from 'ant-design-vue'

const WEEK_LABELS = ['一', '二', '三', '四', '五', '六', '日']

const viewMode = ref<'day' | 'week' | 'month'>('day')
const currentDate = ref(new Date())
const loading = ref(false)
const schedules = ref<DoctorScheduleViewVO[]>([])
const loadedMonths = new Set<string>()

const selectedDate = computed(() => fmtDate(currentDate.value))

/** 日视图分组（按开始时间归类上午 / 下午 / 晚上，其余归「其他」） */
const dayGroups = computed(() => {
  const groups = [
    { label: '上午', items: [] as DoctorScheduleViewVO[] },
    { label: '下午', items: [] as DoctorScheduleViewVO[] },
    { label: '晚上', items: [] as DoctorScheduleViewVO[] },
    { label: '其他', items: [] as DoctorScheduleViewVO[] }
  ]
  for (const item of daySchedules.value) {
    const hour = Number((item.startTime || '').slice(0, 2)) || 0
    const idx = hour < 12 ? 0 : hour < 18 ? 1 : hour < 24 ? 2 : 3
    groups[idx].items.push(item)
  }
  return groups
})

const daySchedules = computed(() => schedulesOf(selectedDate.value))

/** 周视图 7 天（周一起始） */
const weekDays = computed(() => {
  const d = new Date(currentDate.value)
  const offset = (d.getDay() + 6) % 7
  d.setDate(d.getDate() - offset)
  const today = fmtDate(new Date())
  return Array.from({ length: 7 }, (_, i) => {
    const date = fmtDate(d)
    const result = { date, weekLabel: `周${WEEK_LABELS[i]}`, isToday: date === today }
    d.setDate(d.getDate() + 1)
    return result
  })
})

/** 月视图格子（含前后补齐，周一起始，固定 6 行） */
const monthCells = computed(() => {
  const y = currentDate.value.getFullYear()
  const m = currentDate.value.getMonth()
  const first = new Date(y, m, 1)
  const offset = (first.getDay() + 6) % 7
  const start = new Date(y, m, 1 - offset)
  const today = fmtDate(new Date())
  const monthKey = `${y}-${String(m + 1).padStart(2, '0')}`
  return Array.from({ length: 42 }, (_, i) => {
    const d = new Date(start)
    d.setDate(start.getDate() + i)
    const date = fmtDate(d)
    return { date, inMonth: date.startsWith(monthKey), isToday: date === today }
  })
})

function schedulesOf(date: string): DoctorScheduleViewVO[] {
  return schedules.value
    .filter((s) => s.scheduleDate === date)
    .sort((a, b) => (a.startTime || '').localeCompare(b.startTime || ''))
}

function fmtDate(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

/** "09:00:00" → "09:00" */
function fmtTime(t?: string): string {
  return t ? t.slice(0, 5) : '-'
}

function statusLabel(status: number): string {
  return status === 0 ? '停诊' : '出诊'
}

function statusColor(status: number): string {
  return status === 0 ? 'default' : 'green'
}

onMounted(() => {
  loadData()
})

/** 按当前视图所需月份加载数据（已加载月份跳过） */
async function loadData() {
  const months = viewMode.value === 'month' ? [selectedDate.value.slice(0, 7)] : monthsInView()
  const need = months.filter((m) => !loadedMonths.has(m))
  if (!need.length) return
  loading.value = true
  try {
    const results = await Promise.all(need.map((m) => getMonthSchedule(m)))
    for (const res of results) {
      for (const item of res.data || []) {
        if (!schedules.value.some((s) => s.scheduleId === item.scheduleId)) {
          schedules.value.push(item)
        }
      }
    }
    need.forEach((m) => loadedMonths.add(m))
  } catch {
    message.error('加载排班失败')
  } finally {
    loading.value = false
  }
}

/** 当前视图覆盖的月份集合（日/周视图可能跨月） */
function monthsInView(): string[] {
  if (viewMode.value === 'day') return [selectedDate.value.slice(0, 7)]
  return [...new Set(weekDays.value.map((d) => d.date.slice(0, 7)))]
}

function prev() {
  shiftDate(viewMode.value === 'day' ? -1 : viewMode.value === 'week' ? -7 : 'month-1')
  loadData()
}

function next() {
  shiftDate(viewMode.value === 'day' ? 1 : viewMode.value === 'week' ? 7 : 'month+1')
  loadData()
}

function shiftDate(delta: number | string) {
  const d = new Date(currentDate.value)
  if (delta === 'month-1') d.setMonth(d.getMonth() - 1)
  else if (delta === 'month+1') d.setMonth(d.getMonth() + 1)
  else d.setDate(d.getDate() + (delta as number))
  currentDate.value = d
}

function goToday() {
  currentDate.value = new Date()
  loadData()
}

/** 月视图点日期切日视图 */
function goDay(date: string) {
  const [y, m, day] = date.split('-').map(Number)
  currentDate.value = new Date(y, m - 1, day)
  viewMode.value = 'day'
  loadData()
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.view-switch {
  margin-bottom: @spacing-base;
}

.current-date {
  font-size: @font-size-base;
  color: @text-color;
}

.day-group {
  margin-bottom: @spacing-lg;

  &:last-child {
    margin-bottom: 0;
  }

  &__title {
    font-size: @font-size-lg;
    font-weight: @font-weight-semibold;
    color: @text-color;
    margin-bottom: @spacing-sm;
  }
}

.schedule-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: @spacing-base;
}

.schedule-card {
  border: 1px solid @border-color;
  border-radius: @border-radius-base;
  padding: @spacing-base;
  background: @component-background;
  display: flex;
  flex-direction: column;
  gap: @spacing-xs;

  &--off {
    opacity: 0.55;
  }

  &__head {
    display: flex;
    align-items: center;
    gap: @spacing-sm;
  }

  &__shift {
    font-weight: @font-weight-medium;
    color: @text-color;
  }

  &__row {
    font-size: @font-size-sm;
    color: @text-color-secondary;
    display: flex;
    align-items: center;
    gap: @spacing-xs;

    &--remark {
      color: @warning-color;
    }
  }
}

.week-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: @spacing-sm;

  &__cell {
    border: 1px solid @border-color;
    border-radius: @border-radius-sm;
    padding: @spacing-sm;
    min-height: 160px;
    display: flex;
    flex-direction: column;
    gap: @spacing-xs;
    background: @component-background;

    &--today {
      border-color: @primary-color;
      background: @primary-color-bg;
    }
  }

  &__date {
    font-size: @font-size-sm;
    color: @text-color-secondary;
    display: flex;
    justify-content: space-between;
  }

  &__date-num {
    font-weight: @font-weight-semibold;
    color: @text-color;
  }

  &__empty {
    color: @text-color-disabled;
    font-size: @font-size-xs;
    text-align: center;
    margin-top: @spacing-lg;
  }
}

.schedule-chip {
  border-left: 3px solid @primary-color;
  background: @primary-color-bg;
  border-radius: @border-radius-sm;
  padding: @spacing-xs @spacing-sm;
  font-size: @font-size-xs;
  color: @text-color;

  &--off {
    border-left-color: @text-color-disabled;
    background: @background-color;
    opacity: 0.7;
  }

  &__time {
    font-weight: @font-weight-medium;
  }

  &__location {
    color: @text-color-secondary;
  }

  &__off-tag {
    color: @error-color;
  }
}

.month-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: @spacing-xs;

  &__head {
    text-align: center;
    font-weight: @font-weight-medium;
    color: @text-color-secondary;
    padding: @spacing-xs 0;
  }

  &__cell {
    min-height: 76px;
    border: 1px solid @border-color;
    border-radius: @border-radius-sm;
    padding: @spacing-xs;
    cursor: pointer;
    transition: border-color 0.2s;

    &:hover {
      border-color: @primary-color-hover;
    }

    &--other {
      opacity: 0.4;
      cursor: default;
    }

    &--today .month-grid__day {
      color: @primary-color;
      font-weight: @font-weight-semibold;
    }

    &--selected {
      border-color: @primary-color;
      background: @primary-color-bg;
    }
  }

  &__day {
    font-size: @font-size-sm;
    color: @text-color;
    margin-bottom: @spacing-xs;
  }
}

.month-dot {
  font-size: @font-size-xs;
  color: @text-color-secondary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  border-left: 2px solid @primary-color;
  padding-left: @spacing-xs;

  &--off {
    border-left-color: @text-color-disabled;
    text-decoration: line-through;
  }

  &--more {
    border-left: none;
  }
}

@media (max-width: 768px) {
  .week-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .month-grid {
    grid-template-columns: repeat(7, minmax(0, 1fr));

    &__cell {
      min-height: 56px;
    }
  }

  .month-dot {
    display: none;
  }
}
</style>
