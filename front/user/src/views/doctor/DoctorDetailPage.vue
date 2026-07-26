<template>
  <div class="page">
    <van-nav-bar title="医生详情" left-arrow @click-left="router.back()" />

    <!-- 医生信息卡片 -->
    <glass-card v-if="doctor" class="doctor-card">
      <div class="doctor-header">
        <van-image round width="72" height="72" :src="doctor.avatar || undefined" />
        <div class="doctor-info">
          <div class="name">{{ doctor.doctorName }}</div>
          <div class="meta">{{ doctor.positionName || doctor.doctorPositionId }} · {{ doctor.degreeName || doctor.degreeId }}</div>
          <div class="dept">{{ doctor.departmentName }}</div>
        </div>
      </div>
      <div v-if="doctor.description" class="desc">{{ doctor.description }}</div>
      <div v-if="doctor.tags" class="tags">
        <van-tag v-for="tag in parseTags(doctor.tags)" :key="tag" type="primary" plain size="medium">
          {{ tag }}
        </van-tag>
      </div>
    </glass-card>

    <!-- 7天排班横向滚动 -->
    <glass-card class="schedule-card">
      <div class="section-title">排班日期</div>
      <van-loading v-if="scheduleLoading" size="24px" />
      <div v-else class="date-scroll">
        <div
          v-for="s in recentSchedule"
          :key="s.date"
          class="date-item"
          :class="{ active: selectedDate === s.date, disabled: s.remainQuota <= 0 }"
          @click="selectDate(s)"
        >
          <div class="date-label">{{ formatShortDate(s.date) }}</div>
          <div class="quota">{{ s.remainQuota > 0 ? `余${s.remainQuota}` : '约满' }}</div>
        </div>
      </div>
    </glass-card>

    <!-- 时段列表 -->
    <glass-card v-if="selectedDate" class="time-card">
      <div class="section-title">{{ selectedDate }} 时段</div>
      <van-loading v-if="timeLoading" size="24px" />
      <empty-state v-else-if="!timeSlots.length" description="暂无时段" />
      <van-grid v-else :column-num="2" :border="false" :gutter="10">
        <van-grid-item
          v-for="slot in timeSlots"
          :key="slot.registrationScheduleId"
          :class="{ active: selectedSlot?.registrationScheduleId === slot.registrationScheduleId }"
          @click="selectSlot(slot)"
        >
          <template #text>
            <div class="slot-time">{{ formatTime(slot.startTime) }}-{{ formatTime(slot.endTime) }}</div>
            <div class="slot-quota">{{ slot.remainQuota > 0 ? `余${slot.remainQuota}` : '约满' }}</div>
          </template>
        </van-grid-item>
      </van-grid>
    </glass-card>

    <!-- 底部预约按钮 -->
    <van-action-bar>
      <van-action-bar-button
        type="primary"
        text="预约挂号"
        :disabled="!selectedSlot"
        @click="goToSchedule"
      />
    </van-action-bar>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import GlassCard from '@/components/GlassCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getDoctorById } from '@/api/doctor'
import { getRecentSchedule, getTimeSlots, getSchedulePrice } from '@/api/registration'
import { useRegistrationStore } from '@/stores/registration'
import type { DoctorVo } from '@/api/doctor'
import type { RegistrationDateAndRemainQuotaVo, RegistrationConfirmTime } from '@/api/registration'
import { formatTime } from '@/utils/format'
import { showToast } from 'vant'

/**
 * 医生详情页
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生信息、7天排班、时段选择、预约挂号
 * @Datetime: 2026-07-18 14:00
 * @Version: 1.0
 */

const router = useRouter()
const route = useRoute()
const regStore = useRegistrationStore()

const doctor = ref<DoctorVo | null>(null)
const scheduleLoading = ref(false)
const timeLoading = ref(false)

const recentSchedule = ref<RegistrationDateAndRemainQuotaVo[]>([])
const timeSlots = ref<RegistrationConfirmTime[]>([])

const selectedDate = ref('')
const selectedSlot = ref<RegistrationConfirmTime | null>(null)

const doctorId = String(route.params.id)

onMounted(async () => {
  if (!doctorId) { router.back(); return }
  await Promise.all([loadDoctor(), loadSchedule()])
})

function parseTags(tags?: string): string[] {
  if (!tags) return []
  return String(tags).split(',').map(t => t.trim()).filter(Boolean)
}

async function loadDoctor() {
  try {
    const res = await getDoctorById(Number(doctorId))
    doctor.value = res.data || null
  } catch {
    showToast('加载医生信息失败')
  }
}

async function loadSchedule() {
  scheduleLoading.value = true
  try {
    const res = await getRecentSchedule(Number(doctorId))
    recentSchedule.value = res.data || []
  } catch {
    showToast('加载排班失败')
  } finally {
    scheduleLoading.value = false
  }
}

async function selectDate(s: RegistrationDateAndRemainQuotaVo) {
  if (s.remainQuota <= 0) { showToast('该日期已约满'); return }
  selectedDate.value = s.date
  selectedSlot.value = null
  timeLoading.value = true
  try {
    const res = await getTimeSlots(Number(doctorId), s.date)
    timeSlots.value = res.data || []
  } catch {
    showToast('加载时段失败')
  } finally {
    timeLoading.value = false
  }
}

function selectSlot(slot: RegistrationConfirmTime) {
  if (slot.remainQuota <= 0) { showToast('该时段已约满'); return }
  selectedSlot.value = slot
}

async function goToSchedule() {
  if (!selectedSlot.value) { showToast('请选择时段'); return }
  // 保存选中医生和时段到 store
  regStore.selectedDoctor = doctor.value

  // 获取价格
  try {
    const res = await getSchedulePrice(selectedSlot.value.registrationScheduleId)
    regStore.setSelectedSchedule({ ...selectedSlot.value, price: res.data || 0 })
  } catch {
    regStore.setSelectedSchedule(selectedSlot.value)
  }

  router.push('/registration/confirm')
}

// U22: "yyyy-MM-dd" 被 new Date 按 UTC 解析，非 UTC+8 时区日期会错位，补 T00:00:00 强制本地时区
function formatShortDate(dateStr: string) {
  const d = new Date(dateStr + 'T00:00:00')
  return `${d.getMonth() + 1}/${d.getDate()}`
}
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 60px;
}

.doctor-card {
  margin: 16px;

  .doctor-header {
    display: flex;
    align-items: center;

    .doctor-info {
      margin-left: 16px;
      flex: 1;

      .name { font-size: 20px; font-weight: 600; }
      .meta { font-size: 14px; color: #666; margin-top: 4px; }
      .dept { font-size: 14px; color: #999; margin-top: 4px; }
    }
  }

  .desc {
    margin-top: 12px;
    font-size: 14px;
    color: #666;
    line-height: 1.6;
  }

  .tags {
    margin-top: 8px;
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}

.schedule-card, .time-card {
  margin: 0 16px 16px;

  .section-title {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 12px;
  }
}

.date-scroll {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.date-item {
  flex-shrink: 0;
  width: 56px;
  text-align: center;
  padding: 10px 4px;
  border-radius: 8px;
  background: #f7f8fa;
  cursor: pointer;

  &.active {
    background: #1989fa;
    color: #fff;

    .quota { color: #fff; }
  }

  &.disabled {
    opacity: 0.5;
  }

  .date-label { font-size: 14px; }
  .quota { font-size: 12px; color: #999; margin-top: 4px; }
}

.time-card {
  .slot-time { font-size: 14px; color: #333; }
  .slot-quota { font-size: 12px; color: #999; margin-top: 4px; }

  :deep(.van-grid-item.active) {
    .van-grid-item__content {
      background: #1989fa;
      .slot-time, .slot-quota { color: #fff; }
    }
  }
}
</style>
