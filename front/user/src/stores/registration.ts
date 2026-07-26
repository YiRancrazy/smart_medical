import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useRegistrationStore = defineStore('registration', () => {
  const selectedDoctor = ref<any>(null)
  // U15: selectedSchedule 持久化到 sessionStorage，刷新后不丢失
  const selectedSchedule = ref<any>(
    sessionStorage.getItem('reg_selectedSchedule')
      ? JSON.parse(sessionStorage.getItem('reg_selectedSchedule')!)
      : null
  )
  const orderId = ref<string | null>(sessionStorage.getItem('reg_orderId'))
  const registrationList = ref<any[]>([])

  function setSelectedSchedule(val: any) {
    selectedSchedule.value = val
    if (val) {
      sessionStorage.setItem('reg_selectedSchedule', JSON.stringify(val))
    } else {
      sessionStorage.removeItem('reg_selectedSchedule')
    }
  }

  function setOrderId(val: string | null) {
    orderId.value = val
    if (val) {
      sessionStorage.setItem('reg_orderId', val)
    } else {
      sessionStorage.removeItem('reg_orderId')
    }
  }

  function resetFlow() {
    selectedDoctor.value = null
    selectedSchedule.value = null
    orderId.value = null
    sessionStorage.removeItem('reg_selectedSchedule')
    sessionStorage.removeItem('reg_orderId')
  }

  return { selectedDoctor, selectedSchedule, orderId, registrationList, setSelectedSchedule, setOrderId, resetFlow }
})
