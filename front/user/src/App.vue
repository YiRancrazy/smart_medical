<template>
  <div class="app">
    <patient-switch-bar />
    <router-view />
  </div>
</template>

<script setup lang="ts">
import PatientSwitchBar from '@/components/PatientSwitchBar.vue'
</script>

<style scoped lang="scss">
.app {
  min-height: 100vh;
}
</style>
