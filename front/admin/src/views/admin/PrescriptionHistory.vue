<template>
  <prescription-history-panel
    title="处方历史"
    role="admin"
    :page-api="pagePrescriptions"
    :detail-api="getPrescriptionDetail"
  />
</template>

<script setup lang="ts">
import PrescriptionHistoryPanel from '@/components/business/PrescriptionHistoryPanel.vue'
import { pagePrescriptions, getPrescriptionDetail } from '@/api/history/prescription'
</script>
