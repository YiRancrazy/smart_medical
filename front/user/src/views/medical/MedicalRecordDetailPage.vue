<template>
  <div class="page">
    <van-nav-bar title="病历详情" left-arrow @click-left="$router.back()" />

    <van-loading v-if="loading" size="24px" />
    <template v-else-if="record">
      <glass-card class="card">
        <div class="section-title">病历信息</div>
        <van-cell title="主诉" :value="record.chiefComplaint" />
        <van-cell title="现病史" :value="record.presentIllness" />
        <van-cell title="既往史" :value="record.pastHistory" />
        <van-cell title="体格检查" :value="record.physicalExam" />
        <van-cell title="诊断" :value="record.diagnosis" />
        <van-cell title="治疗方案" :value="record.treatmentPlan" />
      </glass-card>

      <glass-card v-if="record.prescriptionId" class="card" is-link @click="$router.push(`/prescription/${record.prescriptionId}`)">
        <div class="section-title">处方信息</div>
        <div class="link-text">点击查看处方详情 →</div>
      </glass-card>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getMedicalRecordDetail } from '@/api/medicalRecord'
import type { MedicalRecord } from '@/api/medicalRecord'
import GlassCard from '@/components/GlassCard.vue'
import { showToast } from 'vant'

const route = useRoute()
const loading = ref(true)
const record = ref<MedicalRecord | null>(null)
const id = String(route.params.id)

onMounted(async () => {
  try {
    const res = await getMedicalRecordDetail(id)
    record.value = res.data || null
  } catch {
    showToast('加载失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.page {
  min-height: 100vh;
  background: $color-bg-page;
  padding-bottom: 24px;
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

.link-text {
  font-size: $font-size-sm;
  color: #1989fa;
}
</style>
