<template>
  <prescription-history-panel
    title="处方历史"
    role="doctor"
    :page-api="pagePrescriptions"
    :detail-api="getPrescriptionDetail"
    :cancel-api="cancelPrescription"
  />
</template>

<script setup lang="ts">
import PrescriptionHistoryPanel from '@/components/business/PrescriptionHistoryPanel.vue'
import { pagePrescriptions, getPrescriptionDetail } from '@/api/history/prescription'
import { cancelPrescription } from '@/api/doctor/prescription'
</script>
