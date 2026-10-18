import type { RouteRecordRaw } from 'vue-router'
import {
  DashboardOutlined,
  TeamOutlined,
  ApartmentOutlined,
  MedicineBoxOutlined,
  CalendarOutlined,
  FileTextOutlined,
  FileDoneOutlined
} from '@ant-design/icons-vue'

export const adminRoutes: RouteRecordRaw = {
  path: '/admin',
  name: 'AdminRoot',
  component: () => import('@/layouts/AdminLayout.vue'),
  meta: { roles: [1] },
  redirect: '/admin/dashboard',
  children: [
    { path: 'dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/Dashboard.vue'), meta: { title: '工作台', icon: DashboardOutlined } },
    { path: 'accounts', name: 'AccountManage', component: () => import('@/views/admin/AccountManage.vue'), meta: { title: '账户管理', icon: TeamOutlined } },
    { path: 'departments', name: 'DepartmentManage', component: () => import('@/views/admin/DepartmentManage.vue'), meta: { title: '科室管理', icon: ApartmentOutlined } },
    { path: 'doctors', name: 'DoctorManage', component: () => import('@/views/admin/DoctorManage.vue'), meta: { title: '医生管理', icon: MedicineBoxOutlined } },
    { path: 'schedule-templates', name: 'ScheduleTemplate', component: () => import('@/views/admin/ScheduleTemplate.vue'), meta: { title: '排班管理', icon: CalendarOutlined } },
    { path: 'medical-records', name: 'AdminMedicalRecordHistory', component: () => import('@/views/admin/MedicalRecordHistory.vue'), meta: { title: '病历历史', icon: FileTextOutlined } },
    { path: 'prescriptions', name: 'AdminPrescriptionHistory', component: () => import('@/views/admin/PrescriptionHistory.vue'), meta: { title: '处方历史', icon: FileDoneOutlined } }
  ]
}
