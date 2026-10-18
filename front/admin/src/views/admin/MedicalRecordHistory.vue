<template>
  <medical-record-history-panel
    title="病历历史"
    role="admin"
    :page-api="pageMedicalRecords"
    :detail-api="getMedicalRecordDetail"
  />
</template>

<script setup lang="ts">
import MedicalRecordHistoryPanel from '@/components/business/MedicalRecordHistoryPanel.vue'
import { pageMedicalRecords, getMedicalRecordDetail } from '@/api/history/medicalRecord'
</script>
