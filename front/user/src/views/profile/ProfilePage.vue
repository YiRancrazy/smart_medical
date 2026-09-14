<template>
  <div class="page">
    <glass-card class="user-card">
      <div class="user-info">
        <van-icon name="user-o" class="avatar" />
        <div class="user-detail">
          <div class="user-name">{{ displayName }}</div>
          <div class="user-card-no">就诊卡号: {{ ownPatientCardSn }}</div>
        </div>
      </div>
    </glass-card>

    <glass-card class="menu-card" padding="0">
      <van-cell title="个人信息" :value="profileStatus" is-link to="/profile/edit" icon="contact-o" />
      <van-cell title="我的挂号" is-link to="/registration" icon="records-o" />
      <van-cell title="我的病历" is-link to="/medical-record" icon="description" />
      <van-cell title="我的处方" is-link to="/prescription" icon="bill-o" />
      <van-cell title="门诊费用" is-link to="/outpatient-fee" icon="balance-list-o" />
      <van-cell title="就诊人管理" is-link to="/patient" icon="friends-o" />
      <van-cell title="修改密码" is-link to="/change-password" icon="setting-o" />
    </glass-card>

    <van-button round block type="danger" plain class="logout-btn" @click="handleLogout">退出登录</van-button>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import { usePatientStore } from '@/stores/patient'
import { getUserBaseInfo } from '@/api/user'
import { showConfirmDialog } from 'vant'
import GlassCard from '@/components/GlassCard.vue'

const userStore = useUserStore()
const patientStore = usePatientStore()

// 展示名与本人卡号：进页实时拉取，优先本人就诊卡姓名，无绑定则回退账号默认昵称
const displayName = ref('-')
const ownPatientCardSn = ref('-')
const profileStatus = computed(() => userStore.profileCompleted ? '已完善' : '待补充')

async function loadBaseInfo() {
  if (!userStore.uid) return
  try {
    const res = await getUserBaseInfo(userStore.uid)
    displayName.value = res.data?.displayName || '-'
    ownPatientCardSn.value = res.data?.ownPatientCardSn || '-'
  } catch {
    // 接口异常时回退到登录缓存的名字，避免展示空白
    displayName.value = userStore.userInfo?.name || '-'
  }
}

onMounted(() => {
  patientStore.init()
  loadBaseInfo()
  userStore.ensureProfileCompleted().catch(() => {
    // 页面展示不因资料状态接口异常中断
  })
})

async function handleLogout() {
  try {
    await showConfirmDialog({ title: '提示', message: '确定退出登录？' })
    await userStore.logoutWithApi()
  } catch { /* cancel */ }
}
</script>

<style scoped lang="scss">
.page {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.user-card {
  .user-info {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .avatar {
    width: 56px;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    color: $color-primary;
    background: rgba(16, 185, 129, 0.1);
    border-radius: 50%;
  }

  .user-name {
    font-size: $font-size-h3;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  .user-card-no {
    margin-top: 4px;
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

.menu-card {
  :deep(.van-cell) {
    padding: 14px 16px;
  }
}

.logout-btn {
  margin-top: 8px;
}
</style>
