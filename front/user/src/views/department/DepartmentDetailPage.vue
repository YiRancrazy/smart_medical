<template>
  <div class="page">
    <van-nav-bar :title="department?.name || '科室详情'" left-arrow @click-left="router.back()" />

    <van-loading v-if="loading" size="24px" />
    <template v-else-if="department">
      <!-- 科室名称 -->
      <glass-card class="dept-header">
        <div class="name">{{ department.name }}</div>
        <van-tag :type="department.status === 1 ? 'success' : 'default'">
          {{ department.status === 1 ? '正常' : '停用' }}
        </van-tag>
      </glass-card>

      <!-- 医生列表 -->
      <glass-card class="doctor-section">
        <div class="section-title">科室医生</div>
        <van-loading v-if="doctorLoading" size="24px" />
        <empty-state v-else-if="!doctors.length" description="暂无医生" />
        <template v-else>
          <div
            v-for="doc in doctors"
            :key="doc.doctorId"
            class="doctor-item"
            @click="router.push(`/doctor/${doc.doctorId}`)"
          >
            <van-image round width="48" height="48" :src="doc.avatar || undefined" />
            <div class="doctor-info">
              <div class="doctor-name">{{ doc.doctorName }} <span class="position">{{ doc.position }}</span></div>
              <div class="department">{{ doc.departmentName }}</div>
            </div>
            <van-tag type="primary" size="medium">
              {{ formatMoney(doc.price) }}
            </van-tag>
          </div>
        </template>
      </glass-card>
    </template>
    <empty-state v-else description="科室不存在" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import GlassCard from '@/components/GlassCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import { getDepartmentById } from '@/api/department'
import { getDoctorRegistrationBaseInfo } from '@/api/doctor'
import type { Department } from '@/api/department'
import type { RegistrationDoctorBaseInfo } from '@/api/doctor'
import { formatMoney } from '@/utils/format'
import { showToast } from 'vant'

/**
 * 科室详情页
 * @Author: YiRanCrazy@gmail.com
 * @Description: 科室信息 + 科室下医生列表
 * @Datetime: 2026-07-18 14:00
 * @Version: 1.0
 */

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const doctorLoading = ref(false)

const department = ref<Department | null>(null)
const doctors = ref<RegistrationDoctorBaseInfo[]>([])

onMounted(async () => {
  const id = Number(route.params.id)
  if (!id) return

  loading.value = true
  try {
    const res = await getDepartmentById(id)
    department.value = res.data || null
  } catch {
    showToast('加载科室失败')
  } finally {
    loading.value = false
  }

  doctorLoading.value = true
  try {
    const res = await getDoctorRegistrationBaseInfo(id)
    doctors.value = res.data || []
  } catch {
    showToast('加载医生列表失败')
  } finally {
    doctorLoading.value = false
  }
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.dept-header {
  margin: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .name {
    font-size: 20px;
    font-weight: 600;
  }
}

.doctor-section {
  margin: 0 16px 16px;

  .section-title {
    font-size: 16px;
    font-weight: 500;
    margin-bottom: 12px;
  }
}

.doctor-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  .doctor-info {
    flex: 1;
    margin-left: 12px;

    .doctor-name {
      font-size: 15px;
      font-weight: 500;

      .position {
        font-size: 13px;
        color: #666;
        font-weight: normal;
      }
    }

    .department {
      font-size: 13px;
      color: #999;
      margin-top: 4px;
    }
  }
}
</style>
