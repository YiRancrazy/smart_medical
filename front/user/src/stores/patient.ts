import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { getPatientSimpleList, setDefaultPatient } from '@/api/patient'
import type { PatientCardSimpleResponse } from '@/api/patient'
import { getSelectedPatientCardId, setSelectedPatientCardId } from '@/utils/storage'
import { showToast } from 'vant'

export const usePatientStore = defineStore('patient', () => {
  const patientList = ref<PatientCardSimpleResponse[]>([])
  const selectedPatientCardId = ref<string | null>(getSelectedPatientCardId())
  const loading = ref(false)
  const initialized = ref(false)

  const currentPatient = computed<PatientCardSimpleResponse | null>(() => {
    if (!selectedPatientCardId.value) return null
    return patientList.value.find(p => p.patientCardId === selectedPatientCardId.value) || null
  })

  const hasPatients = computed(() => patientList.value.length > 0)

  async function loadPatients() {
    loading.value = true
    try {
      const res = await getPatientSimpleList()
      patientList.value = res.data || []
      ensureSelected()
      initialized.value = true
    } catch {
      showToast('就诊人加载失败')
    } finally {
      loading.value = false
    }
  }

  function ensureSelected() {
    if (patientList.value.length === 0) {
      selectedPatientCardId.value = null
      setSelectedPatientCardId(null)
      return
    }
    const saved = selectedPatientCardId.value
    if (saved && patientList.value.some(p => p.patientCardId === saved)) {
      return
    }
    const def = patientList.value.find(p => p.defaultPatient)
    selectPatient(def || patientList.value[0], false)
  }

  function selectPatient(patient: PatientCardSimpleResponse | null, persist = true) {
    selectedPatientCardId.value = patient?.patientCardId || null
    if (persist) {
      setSelectedPatientCardId(selectedPatientCardId.value)
    }
  }

  async function setDefault(patient: PatientCardSimpleResponse) {
    try {
      await setDefaultPatient(patient.userPatientRelationId)
      patientList.value.forEach(p => {
        p.defaultPatient = p.patientCardId === patient.patientCardId
      })
    } catch {
      showToast('设置默认就诊人失败')
    }
  }

  async function init() {
    if (!initialized.value && !loading.value) {
      await loadPatients()
    }
  }

  // U19: 供 userStore.logout 调用，清空就诊人列表与选中态，避免换账号后残留上一用户数据
  function reset() {
    patientList.value = []
    selectedPatientCardId.value = null
    setSelectedPatientCardId(null)
    initialized.value = false
  }

  return {
    patientList,
    selectedPatientCardId,
    currentPatient,
    hasPatients,
    loading,
    initialized,
    loadPatients,
    selectPatient,
    setDefault,
    init,
    ensureSelected,
    reset
  }
})
