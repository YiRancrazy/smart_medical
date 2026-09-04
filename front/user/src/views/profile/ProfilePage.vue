<template>
  <div class="page">
    <glass-card class="user-card">
      <div class="user-info">
        <van-icon name="user-o" class="avatar" />
        <div class="user-detail">
          <div class="user-name">{{ userStore.userInfo?.name || '-' }}</div>
          <div class="user-card-no">就诊卡号: {{ patientStore.currentPatient?.patientCardSn || '-' }}</div>
        </div>
      </div>
    </glass-card>

    <glass-card class="menu-card" padding="0">
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
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { usePatientStore } from '@/stores/patient'
import { showConfirmDialog } from 'vant'
import GlassCard from '@/components/GlassCard.vue'

const userStore = useUserStore()
const patientStore = usePatientStore()

onMounted(() => {
  patientStore.init()
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
