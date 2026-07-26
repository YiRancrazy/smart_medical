import type { RouteRecordRaw } from 'vue-router'
import { MedicineBoxOutlined, FileTextOutlined, WarningOutlined, FileDoneOutlined } from '@ant-design/icons-vue'

export const pharmacistRoutes: RouteRecordRaw = {
  path: '/pharmacy',
  name: 'PharmacyRoot',
  component: () => import('@/layouts/PharmacistLayout.vue'),
  meta: { roles: [6] },
  redirect: '/pharmacy/pending',
  children: [
    { path: 'pending', name: 'PendingPrescription', component: () => import('@/views/pharmacy/PendingPrescription.vue'), meta: { title: '待发药', icon: MedicineBoxOutlined } },
    { path: 'prescription/:id', name: 'PrescriptionDetail', component: () => import('@/views/pharmacy/PrescriptionDetail.vue'), meta: { title: '处方详情', icon: FileTextOutlined } },
    { path: 'low-stock', name: 'LowStockAlert', component: () => import('@/views/pharmacy/LowStockAlert.vue'), meta: { title: '库存预警', icon: WarningOutlined } },
    { path: 'medical-records', name: 'PharmacyMedicalRecordHistory', component: () => import('@/views/pharmacy/MedicalRecordHistory.vue'), meta: { title: '病历历史', icon: FileTextOutlined } },
    { path: 'prescriptions', name: 'PharmacyPrescriptionHistory', component: () => import('@/views/pharmacy/PrescriptionHistory.vue'), meta: { title: '处方历史', icon: FileDoneOutlined } }
  ]
}
