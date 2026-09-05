<template>
  <div class="consultation-page">
    <van-nav-bar title="在线咨询" left-arrow @click-left="router.back()" />

    <van-loading v-if="loading" class="loading" size="24px" />
    <template v-else>
      <empty-state v-if="!doctors.length" description="暂无可咨询的医生" />
      <div v-else class="doctor-list">
        <div
          v-for="doc in doctors"
          :key="doc.doctorId"
          class="doctor-item"
          @click="router.push(`/chat/${doc.doctorId}?doctorName=${encodeURIComponent(doc.doctorName)}`)"
        >
          <van-image round width="48" height="48" :src="doc.avatar || undefined" />
          <div class="doctor-info">
            <div class="doctor-name">
              {{ doc.doctorName }} <span class="position">{{ doc.position }}</span>
            </div>
            <div class="department">{{ doc.departmentName }}</div>
          </div>
          <van-button type="primary" size="small" plain>咨询</van-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import EmptyState from '@/components/EmptyState.vue'
import { getChildBaseInfoList } from '@/api/department'
import { getDoctorRegistrationBaseInfo } from '@/api/doctor'
import type { RegistrationDoctorBaseInfo } from '@/api/doctor'

const router = useRouter()
const loading = ref(false)
const doctors = ref<RegistrationDoctorBaseInfo[]>([])

onMounted(async () => {
  loading.value = true
  try {
    const res = await getChildBaseInfoList()
    const children = res.data || []
    const all = await Promise.all(
      children.map(async (child) => {
        try {
          const r = await getDoctorRegistrationBaseInfo(child.id)
          return r.data || []
        } catch {
          return [] as RegistrationDoctorBaseInfo[]
        }
      })
    )
    doctors.value = all.flat()
  } catch {
    showToast('加载医生列表失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped lang="scss">
.consultation-page {
  min-height: 100vh;
  background: #f5f5f5;
}

.loading {
  margin: 40px auto;
}

.doctor-list {
  padding: 16px;
}

.doctor-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  margin-bottom: 12px;

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