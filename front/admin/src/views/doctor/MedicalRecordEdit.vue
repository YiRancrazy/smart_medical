<template>
  <page-container :title="pageTitle" :sub-title="patientInfoSubtitle">
    <glass-card>
      <a-spin :spinning="loading">
        <a-form :model="formData" layout="vertical">
          <a-form-item label="主诉" required>
            <a-textarea v-model:value="formData.chiefComplaint" :rows="2" placeholder="请输入主诉" />
          </a-form-item>
          <a-form-item label="现病史" required>
            <a-textarea v-model:value="formData.presentIllness" :rows="3" placeholder="请输入现病史" />
          </a-form-item>
          <a-form-item label="既往史">
            <a-textarea v-model:value="formData.pastHistory" :rows="2" placeholder="请输入既往史" />
          </a-form-item>
          <a-form-item label="查体">
            <a-textarea v-model:value="formData.physicalExam" :rows="2" placeholder="请输入查体结果" />
          </a-form-item>
          <a-form-item label="诊断" required>
            <a-textarea v-model:value="formData.diagnosis" :rows="2" placeholder="请输入诊断" />
          </a-form-item>
          <a-form-item label="治疗方案" required>
            <a-textarea v-model:value="formData.treatmentPlan" :rows="2" placeholder="请输入治疗方案" />
          </a-form-item>

          <!-- 处方药品 -->
          <a-divider>处方药品</a-divider>
          <div v-for="(item, index) in prescriptionItems" :key="item._uid" class="prescription-item">
            <a-row :gutter="12">
              <a-col :span="9">
                <a-form-item label="药品">
                  <a-select
                    v-model:value="item.drugSelectValue"
                    show-search
                    placeholder="输入药品名称搜索"
                    :filter-option="false"
                    :options="item.drugOptions"
                    :loading="item.drugLoading"
                    @search="(kw: string) => handleDrugSearch(index, kw)"
                    @change="(val: number) => handleDrugSelected(index, val)"
                    style="width: 100%"
                  />
                </a-form-item>
              </a-col>
              <a-col :span="5">
                <a-form-item label="数量">
                  <a-input-number v-model:value="item.quantity" :min="1" placeholder="数量" style="width: 100%" />
                </a-form-item>
              </a-col>
              <a-col :span="7">
                <a-form-item label="用法">
                  <a-input v-model:value="item.usageMethod" placeholder="如：口服、一日三次" />
                </a-form-item>
              </a-col>
              <a-col :span="3" style="padding-top: 30px">
                <a-button type="link" danger @click="removeItem(index)">删除</a-button>
              </a-col>
            </a-row>
          </div>
          <a-button type="dashed" block @click="addItem" style="margin-bottom: 16px">
            + 添加药品
          </a-button>

          <a-space>
            <a-button @click="handleSaveDraft" :loading="saving" :disabled="isSubmitted">保存草稿</a-button>
            <a-button type="primary" @click="handleSubmit" :loading="submitting" :disabled="isSubmitted">
              {{ isSubmitted ? '已提交' : '提交病历' }}
            </a-button>
          </a-space>
        </a-form>
      </a-spin>
    </glass-card>

    <!-- 提交结果弹窗 -->
    <a-modal
      v-model:open="resultVisible"
      title="提交成功"
      :footer="null"
    >
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="病历ID">{{ submitResult.medicalRecordId }}</a-descriptions-item>
        <a-descriptions-item label="处方ID">{{ submitResult.prescriptionId }}</a-descriptions-item>
        <a-descriptions-item label="订单号">{{ submitResult.orderSn }}</a-descriptions-item>
        <a-descriptions-item label="总金额">{{ formatAmount(submitResult.totalAmount) }}</a-descriptions-item>
        <a-descriptions-item label="挂号状态">{{ submitResult.registrationStatus }}</a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </page-container>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '@/components/common/PageContainer.vue'
import GlassCard from '@/components/common/GlassCard.vue'
import {
  getMedicalRecord,
  saveDraft,
  submitMedicalRecord
} from '@/api/doctor/medicalRecord'
import type {
  MedicalRecordDetailVO,
  PrescriptionItemRequest,
  PrescriptionSubmitVO
} from '@/api/doctor/medicalRecord'
import { formatMoney } from '@/utils/format'
import { message } from 'ant-design-vue'
import { searchDrugs } from '@/api/doctor/drug'
import type { DrugVO } from '@/api/doctor/drug'

const route = useRoute()
const router = useRouter()
const registrationId = String(route.query.registrationId || '')

// F16: 缺失 registrationId 时直接退回，避免空串提交后端 500
if (!route.query.registrationId) {
  message.error('缺少挂号记录参数')
  router.back()
}

const loading = ref(false)
const saving = ref(false)
const submitting = ref(false)
const recordStatus = ref<number | undefined>(undefined)
const isSubmitted = computed(() => recordStatus.value === 1)

const formData = ref({
  chiefComplaint: '',
  presentIllness: '',
  pastHistory: '',
  physicalExam: '',
  diagnosis: '',
  treatmentPlan: ''
})

const prescriptionItems = ref<PrescriptionItemRequest[]>([])

const patientName = ref('')
const patientPhone = ref('')

const pageTitle = computed(() => '病历编辑')
const patientInfoSubtitle = computed(() => {
  if (patientName.value || patientPhone.value) {
    return `患者：${patientName.value || ''} ${patientPhone.value || ''}`.trim()
  }
  return ''
})

const resultVisible = ref(false)
const submitResult = ref<PrescriptionSubmitVO>({
  medicalRecordId: 0,
  prescriptionId: 0,
  orderId: 0,
  orderSn: '',
  totalAmount: 0,
  registrationStatus: 0
})

onMounted(() => {
  patientName.value = String(route.query.patientName || '')
  patientPhone.value = String(route.query.patientPhone || '')
  if (registrationId) {
    loadRecord()
  }
})

async function loadRecord() {
  loading.value = true
  try {
    const res = await getMedicalRecord(registrationId)
    const data: MedicalRecordDetailVO = res.data
    if (data) {
      formData.value = {
        chiefComplaint: data.chiefComplaint || '',
        presentIllness: data.presentIllness || '',
        pastHistory: data.pastHistory || '',
        physicalExam: data.physicalExam || '',
        diagnosis: data.diagnosis || '',
        treatmentPlan: data.treatmentPlan || ''
      }
      patientName.value = data.patientName || patientName.value
      patientPhone.value = data.patientPhone || patientPhone.value
      recordStatus.value = data.status
    }
  } catch {
    message.error('加载病历失败')
  } finally {
    loading.value = false
  }
}

function addItem() {
  prescriptionItems.value.push({
    _uid: ++uidSeq,
    drugId: undefined as unknown as number,
    quantity: 1,
    usageMethod: '',
    drugSelectValue: undefined,
    drugOptions: [],
    drugLoading: false,
    timer: null
  })
}

function removeItem(index: number) {
  prescriptionItems.value.splice(index, 1)
}

let uidSeq = 0
async function handleDrugSearch(index: number, keyword: string) {
  const item = prescriptionItems.value[index]
  item.drugLoading = true
  // M21: 每行独立定时器，避免一行搜索清除另一行的防抖导致其 loading 永久为 true
  if (item.timer) {
    clearTimeout(item.timer)
  }
  item.timer = setTimeout(async () => {
    try {
      const res = await searchDrugs(keyword)
      const drugs = res.data || []
      item.drugOptions = drugs.map((d: DrugVO) => ({
        label: `${d.commonName} ${d.specification || ''} ${d.unit || ''}`.trim(),
        value: d.id
      }))
    } finally {
      item.drugLoading = false
      item.timer = null
    }
  }, 300)
}

// F24: 卸载时清理各行药品搜索定时器，避免更新已卸载组件
onUnmounted(() => {
  prescriptionItems.value.forEach((item) => {
    if (item.timer) clearTimeout(item.timer)
  })
})

function handleDrugSelected(index: number, val: number) {
  const item = prescriptionItems.value[index]
  item.drugId = val
}

function buildRequest() {
  return {
    registrationId,
    chiefComplaint: formData.value.chiefComplaint,
    presentIllness: formData.value.presentIllness,
    pastHistory: formData.value.pastHistory,
    physicalExam: formData.value.physicalExam,
    diagnosis: formData.value.diagnosis,
    treatmentPlan: formData.value.treatmentPlan
  }
}

async function handleSaveDraft() {
  if (!formData.value.chiefComplaint) {
    message.warning('请填写主诉')
    return
  }
  saving.value = true
  try {
    await saveDraft(buildRequest())
    message.success('草稿已保存')
  } catch {
    message.error('保存草稿失败')
  } finally {
    saving.value = false
  }
}

async function handleSubmit() {
  // F17: 现病史模板标了 required，校验对齐
  if (!formData.value.chiefComplaint || !formData.value.presentIllness || !formData.value.diagnosis || !formData.value.treatmentPlan) {
    message.warning('请填写必填项（主诉、现病史、诊断、治疗方案）')
    return
  }
  // 校验处方药品：若填了药品则 drugId/quantity 必填
  const invalid = prescriptionItems.value.find(
    item => item.drugSelectValue != null && (!item.drugId || !item.quantity || item.quantity < 1)
  )
  if (invalid) {
    message.warning('处方药品请补全药品和数量')
    return
  }
  submitting.value = true
  try {
    const req = {
      ...buildRequest(),
      items: prescriptionItems.value
        .filter(item => item.drugId && item.quantity)
        .map(({ drugId, quantity, usageMethod }) => ({ drugId, quantity, usageMethod }))
    }
    const res = await submitMedicalRecord(req)
    message.success('提交成功')
    // F12: 提交成功后锁定表单，避免重复提交生成重复处方与订单
    recordStatus.value = 1
    if (res.data) {
      submitResult.value = res.data
      resultVisible.value = true
    }
  } catch {
    // M20: 业务错误已在拦截器统一提示，避免二次 toast
  } finally {
    submitting.value = false
  }
}

// F10: 后端 totalAmount 单位为"分"，与 PendingPrescription 一致，需 /100 后再格式化
function formatAmount(amount: number) {
  return formatMoney(amount == null ? undefined : amount / 100)
}
</script>

<style scoped>
.prescription-item {
  margin-bottom: 8px;
}
</style>
