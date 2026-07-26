import type { RouteRecordRaw } from 'vue-router'
import { ScheduleOutlined, UserOutlined, FileTextOutlined, CloseCircleOutlined, FileDoneOutlined } from '@ant-design/icons-vue'

export const doctorRoutes: RouteRecordRaw = {
  path: '/doctor',
  name: 'DoctorRoot',
  component: () => import('@/layouts/DoctorLayout.vue'),
  meta: { roles: [2] },
  redirect: '/doctor/schedule',
  children: [
    { path: 'schedule', name: 'TodaySchedule', component: () => import('@/views/doctor/TodaySchedule.vue'), meta: { title: '今日排班', icon: ScheduleOutlined } },
    { path: 'waiting', name: 'WaitingList', component: () => import('@/views/doctor/WaitingList.vue'), meta: { title: '候诊列表', icon: UserOutlined } },
    { path: 'in-progress', name: 'InProgressList', component: () => import('@/views/doctor/InProgressList.vue'), meta: { title: '就诊中', icon: UserOutlined } },
    { path: 'medical-record', name: 'MedicalRecordEdit', component: () => import('@/views/doctor/MedicalRecordEdit.vue'), meta: { title: '病历编辑', icon: FileTextOutlined } },
    { path: 'prescription-cancel', name: 'PrescriptionCancel', component: () => import('@/views/doctor/PrescriptionCancel.vue'), meta: { title: '处方作废', icon: CloseCircleOutlined } },
    { path: 'medical-records', name: 'DoctorMedicalRecordHistory', component: () => import('@/views/doctor/MedicalRecordHistory.vue'), meta: { title: '病历历史', icon: FileTextOutlined } },
    { path: 'prescriptions', name: 'DoctorPrescriptionHistory', component: () => import('@/views/doctor/PrescriptionHistory.vue'), meta: { title: '处方历史', icon: FileDoneOutlined } }
  ]
}
