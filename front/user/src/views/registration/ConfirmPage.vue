<template>
  <div class="page">
    <van-nav-bar title="预约确认" left-arrow @click-left="onBack" />

    <!-- 医生信息 -->
    <glass-card v-if="doctor" class="card">
      <div class="section-title">医生信息</div>
      <van-cell-group inset>
        <van-cell title="就诊科室" :value="doctor.departmentName" />
        <van-cell title="就诊医生" :value="doctor.name" />
        <van-cell title="医生职称" :value="doctor.positionName" />
      </van-cell-group>
    </glass-card>

    <!-- 挂号信息 -->
    <glass-card v-if="schedule" class="card">
      <div class="section-title">挂号信息</div>
      <van-cell-group inset>
        <van-cell title="就诊日期" :value="formatDate(schedule.startTime)" />
        <van-cell title="就诊时段" :value="`${formatTime(schedule.startTime)} - ${formatTime(schedule.endTime)}`" />
        <van-cell title="挂号费用">
          <template #value>
            <span class="price">{{ formatMoney(schedule.price) }}</span>
          </template>
        </van-cell>
      </van-cell-group>
    </glass-card>

    <!-- 就诊人选择 -->
    <glass-card class="card">
      <div class="section-title">选择就诊人</div>
      <van-loading v-if="patientLoading" size="24px" />
      <van-empty v-else-if="!patients.length" description="暂无就诊人" />
      <van-radio-group v-else v-model="selectedPatientId">
        <van-cell-group inset>
          <van-cell
            v-for="p in patients"
            :key="p.patientCardNo"
            :title="p.patientName"
            :label="`${p.patientPhone} | ${p.patientIdCard}`"
            clickable
            @click="selectedPatientId = p.patientCardNo"
          >
            <template #right-icon>
              <van-radio :name="p.patientCardNo" />
            </template>
          </van-cell>
        </van-cell-group>
      </van-radio-group>
    </glass-card>

    <!-- 底部 -->
    <van-action-bar>
      <van-action-bar-button
        type="primary"
        text="确认预约"
        :loading="submitting"
        :disabled="!canSubmit"
        @click="handleSubmit"
      />
    </van-action-bar>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import GlassCard from '@/components/GlassCard.vue'
import { submitRegistration } from '@/api/registration'
import { getDoctorRegistrationConfirm } from '@/api/doctor'
import { getConfirmPatientBaseInfo } from '@/api/patient'
import { getDefaultPaymentMethod } from '@/api/payment'
import type { RegistrationDoctorConfirmVo } from '@/api/doctor'
import type { RegistrationConfirmTime } from '@/api/registration'
import type { RegistrationConfirmPatientCardVo } from '@/api/patient'
import { useRegistrationStore } from '@/stores/registration'
import { useUserStore } from '@/stores/user'
import { usePatientStore } from '@/stores/patient'
import { formatMoney, formatTime, formatDate } from '@/utils/format'
import { showToast } from 'vant'

/**
 * 预约确认页面
 * @Author: YiRanCrazy@gmail.com
 * @Description: 确认预约信息、选择就诊人、提交预约
 * @Datetime: 2026-07-18 10:00
 * @Version: 1.0
 */

const router = useRouter()
const regStore = useRegistrationStore()
const userStore = useUserStore()
const patientStore = usePatientStore()

const doctor = ref<RegistrationDoctorConfirmVo | null>(null)
const schedule = computed(() => regStore.selectedSchedule as (RegistrationConfirmTime & { price?: number }) | null)

const patientLoading = ref(false)
const patients = ref<RegistrationConfirmPatientCardVo[]>([])
const selectedPatientId = ref<string | null>(null)

const submitting = ref(false)
const defaultPaymentMethodId = ref<string>('1')
const canSubmit = computed(() => selectedPatientId.value && !submitting.value)

onMounted(async () => {
  // U15: regStore.selectedSchedule 为内存态，刷新后丢失；store 初始化已尝试从 sessionStorage 恢复，
  // 此处再兜底一次：若仍为 null 则直接读 sessionStorage 恢复到 store
  if (!regStore.selectedSchedule) {
    const cached = sessionStorage.getItem('reg_selectedSchedule')
    if (cached) {
      try {
        regStore.setSelectedSchedule(JSON.parse(cached))
      } catch {
        // JSON 解析失败忽略
      }
    }
  }
  if (!regStore.selectedSchedule) {
    showToast('请先选择排班')
    router.replace('/registration')
    return
  }
  await Promise.all([loadDoctor(), loadPatients(), loadDefaultPaymentMethod()])
})

async function loadDefaultPaymentMethod() {
  try {
    const res = await getDefaultPaymentMethod()
    if (res.data?.id) {
      defaultPaymentMethodId.value = String(res.data.id)
    }
  } catch {
    // 无默认则使用兜底值
  }
}

async function loadDoctor() {
  const doctorId = regStore.selectedDoctor?.doctorId ?? regStore.selectedDoctor?.id
  if (!doctorId) return
  try {
    const res = await getDoctorRegistrationConfirm(doctorId)
    doctor.value = res.data || null
  } catch {
    showToast('加载医生信息失败')
  }
}

async function loadPatients() {
  patientLoading.value = true
  try {
    const res = await getConfirmPatientBaseInfo()
    patients.value = res.data || []
    applyCurrentPatient()
  } catch {
    showToast('加载就诊人失败')
  } finally {
    patientLoading.value = false
  }
}

function applyCurrentPatient() {
  const storeCardId = patientStore.currentPatient?.patientCardId
  if (storeCardId && patients.value.some(p => p.patientCardNo === storeCardId)) {
    selectedPatientId.value = storeCardId
    return
  }
  const def = patients.value.find(p => p.defaultPatientCard)
  selectedPatientId.value = def?.patientCardNo || patients.value[0]?.patientCardNo || null
}

watch(() => patientStore.currentPatient, applyCurrentPatient)

async function handleSubmit() {
  if (!selectedPatientId.value) {
    showToast('请选择就诊人')
    return
  }
  // M19: 排班在 sessionStorage 中被清空后直接返回，避免非空断言异常
  if (!regStore.selectedSchedule) {
    showToast('排班信息已失效，请重新选择')
    router.replace('/registration/doctor')
    return
  }
  submitting.value = true
  try {
    const res = await submitRegistration({
      paymentMethodId: defaultPaymentMethodId.value,
      registrationScheduleId: regStore.selectedSchedule.registrationScheduleId,
      userId: userStore.uid || '',
      patientCardId: selectedPatientId.value
    })
    if (res.data) {
      showToast('预约成功')
      regStore.setOrderId(res.data)
      const amount = regStore.selectedSchedule?.price || 0
      // U15: 预约已提交，排班已消费，清除 selectedSchedule 防止残留（金额已通过 URL 传递给支付页）
      regStore.setSelectedSchedule(null)
      router.replace(`/registration/payment?orderId=${res.data}&amount=${amount}`)
    }
  } catch {
    // M20: 业务错误已在拦截器统一提示，避免二次 toast
  } finally {
    submitting.value = false
  }
}

/** 返回上一页（取消预约），清除已选排班防止残留 */
function onBack() {
  // U15: 取消则清除已选排班
  regStore.setSelectedSchedule(null)
  router.back()
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $color-bg-page;
  padding-bottom: 60px;
}

.card {
  margin: 16px;
}

.section-title {
  font-size: $font-size-h3;
  font-weight: $font-weight-semibold;
  margin-bottom: 12px;
  color: $color-text-primary;
}

.price {
  color: #ff5722;
  font-size: 18px;
  font-weight: 600;
}
</style>
