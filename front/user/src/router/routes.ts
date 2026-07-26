import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  // ── 公开页 ──────────────────────────────────────
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginPage.vue'),
    meta: { requiresAuth: false, title: '登录' }
  },

  // ── TabBar 主目的地（首页/挂号/就诊人/我的）─────
  // 仅这 4 个页面展示底部 TabBar，其余聚焦流程页不展示
  {
    path: '/',
    component: () => import('@/components/AppTabBar.vue'),
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/home/HomePage.vue'),
        meta: { requiresAuth: true, title: '首页' }
      },
      {
        path: 'registration',
        name: 'RegistrationList',
        component: () => import('@/views/registration/RegistrationListPage.vue'),
        meta: { requiresAuth: true, title: '我的挂号' }
      },
      {
        path: 'patient',
        name: 'PatientList',
        component: () => import('@/views/patient/PatientListPage.vue'),
        meta: { requiresAuth: true, title: '就诊人' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfilePage.vue'),
        meta: { requiresAuth: true, title: '我的' }
      }
    ]
  },

  // ── 挂号流程页（无 TabBar，聚焦操作）────────────
  {
    path: '/department',
    name: 'DepartmentList',
    component: () => import('@/views/department/DepartmentListPage.vue'),
    meta: { requiresAuth: true, title: '科室列表' }
  },
  {
    path: '/department/:id',
    name: 'DepartmentDetail',
    component: () => import('@/views/department/DepartmentDetailPage.vue'),
    meta: { requiresAuth: true, title: '科室详情' }
  },
  {
    path: '/doctor/:id',
    name: 'DoctorDetail',
    component: () => import('@/views/doctor/DoctorDetailPage.vue'),
    meta: { requiresAuth: true, title: '医生详情' }
  },
  {
    path: '/registration/confirm',
    name: 'RegistrationConfirm',
    component: () => import('@/views/registration/ConfirmPage.vue'),
    meta: { requiresAuth: true, title: '预约确认' }
  },
  {
    path: '/registration/payment',
    name: 'RegistrationPayment',
    component: () => import('@/views/registration/PaymentPage.vue'),
    meta: { requiresAuth: true, title: '支付' }
  },
  {
    path: '/checkin/:id',
    name: 'CheckIn',
    component: () => import('@/views/checkin/CheckInPage.vue'),
    meta: { requiresAuth: true, title: '报到' }
  },

  // ── 就诊人编辑（无 TabBar）──────────────────────
  {
    path: '/patient/edit',
    name: 'PatientEdit',
    component: () => import('@/views/patient/PatientEditPage.vue'),
    meta: { requiresAuth: true, title: '编辑就诊人' }
  },

  // ── 病历/处方浏览（无 TabBar）───────────────────
  {
    path: '/medical-record',
    name: 'MedicalRecordList',
    component: () => import('@/views/medical/MedicalRecordListPage.vue'),
    meta: { requiresAuth: true, title: '我的病历' }
  },
  {
    path: '/medical-record/:id',
    name: 'MedicalRecordDetail',
    component: () => import('@/views/medical/MedicalRecordDetailPage.vue'),
    meta: { requiresAuth: true, title: '病历详情' }
  },
  {
    path: '/prescription',
    name: 'PrescriptionList',
    component: () => import('@/views/medical/PrescriptionListPage.vue'),
    meta: { requiresAuth: true, title: '我的处方' }
  },
  {
    path: '/prescription/:id',
    name: 'PrescriptionDetail',
    component: () => import('@/views/medical/PrescriptionDetailPage.vue'),
    meta: { requiresAuth: true, title: '处方详情' }
  },

  // ── 在线咨询（无 TabBar）────────────────────────
  {
    path: '/consultation',
    name: 'Consultation',
    component: () => import('@/views/consultation/ConsultationPage.vue'),
    meta: { requiresAuth: true, title: '在线咨询' }
  },

  // ── 门诊费用（无 TabBar）────────────────────────
  {
    path: '/outpatient-fee',
    name: 'OutpatientFee',
    component: () => import('@/views/profile/OutpatientFeePage.vue'),
    meta: { requiresAuth: true, title: '门诊费用' }
  },

  // ── 404 兜底 ────────────────────────────────────
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/NotFoundPage.vue'),
    meta: { requiresAuth: false, title: '页面不存在' }
  }
]

export default routes
