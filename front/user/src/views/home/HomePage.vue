<template>
  <div class="home-page">
    <header class="home-header">
      <div class="hospital-info">
        <van-icon name="cluster-o" class="hospital-icon" />
        <span class="hospital-name">智慧医疗互联网医院</span>
      </div>
      <div class="header-actions">
        <van-icon name="scan" />
        <van-icon name="setting-o" />
      </div>
    </header>

    <glass-card class="search-card" padding="8px 12px" radius="12px">
      <van-search v-model="keyword" placeholder="搜索科室或医生" @search="handleSearch" />
    </glass-card>

    <glass-card class="banner-card" padding="0" radius="16px">
      <div class="banner-content">
        <div class="banner-text">
          <div class="banner-title">互联网医院<br>使用操作指南</div>
          <div class="banner-btn">点击查看</div>
        </div>
        <van-icon name="user-o" class="banner-icon" />
      </div>
    </glass-card>

    <div class="top-services">
      <div
        v-for="svc in topServices"
        :key="svc.label"
        class="top-service-card"
        :style="{ background: svc.bg }"
        @click="handleService(svc)"
      >
        <div class="top-service-icon" :style="{ background: svc.iconBg }">
          <van-icon :name="svc.icon" />
        </div>
        <div class="top-service-text">
          <div class="top-service-title">{{ svc.label }}</div>
          <div class="top-service-desc">{{ svc.desc }}</div>
        </div>
      </div>
    </div>

    <glass-card class="quick-card">
      <van-grid :column-num="4" :border="false">
        <van-grid-item v-for="q in quickItems" :key="q.label" @click="handleService(q)">
          <template #icon>
            <div class="quick-icon-wrap" :style="{ background: q.bg }">
              <van-icon :name="q.icon" />
            </div>
          </template>
          <template #text>
            <span class="quick-text">{{ q.label }}</span>
          </template>
        </van-grid-item>
      </van-grid>
    </glass-card>

    <glass-card class="tab-card" padding="0">
      <van-tabs v-model:active="activeTab" class="service-tabs">
        <van-tab title="门诊服务">
          <van-grid :column-num="4" :border="false" class="tab-grid">
            <van-grid-item v-for="s in outpatientServices" :key="s.label" @click="handleService(s)">
              <template #icon>
                <div class="tab-icon-wrap" :style="{ background: s.bg }">
                  <van-icon :name="s.icon" />
                </div>
              </template>
              <template #text>
                <span class="tab-text">{{ s.label }}</span>
              </template>
            </van-grid-item>
          </van-grid>
        </van-tab>
        <van-tab title="住院服务">
          <van-grid :column-num="4" :border="false" class="tab-grid">
            <van-grid-item v-for="s in inpatientServices" :key="s.label" @click="handleService(s)">
              <template #icon>
                <div class="tab-icon-wrap" :style="{ background: s.bg }">
                  <van-icon :name="s.icon" />
                </div>
              </template>
              <template #text>
                <span class="tab-text">{{ s.label }}</span>
              </template>
            </van-grid-item>
          </van-grid>
        </van-tab>
      </van-tabs>
    </glass-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import GlassCard from '@/components/GlassCard.vue'
import { showToast } from 'vant'

/**
 * 用户首页
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医院首页：搜索、banner、服务入口、门诊/住院服务
 * @Datetime: 2026-07-17 11:50
 * @Version: 1.0
 */

const router = useRouter()
const keyword = ref('')
const activeTab = ref(0)

interface ServiceItem {
  label: string
  desc?: string
  icon: string
  bg: string
  iconBg: string
  path?: string
}

const topServices: ServiceItem[] = [
  { label: '即刻就诊', desc: '医生30分钟内接诊', icon: 'clock-o', bg: '#FFF7ED', iconBg: '#FDBA74' },
  { label: '预约就诊', desc: '医生24小时内接诊', icon: 'calendar-o', bg: '#ECFDF5', iconBg: '#34D399', path: '/department' },
  { label: '在线咨询', desc: '在线医师快速答复', icon: 'chat-o', bg: '#EFF6FF', iconBg: '#60A5FA', path: '/consultation' },
  { label: '便民门诊', desc: '足不出户线上续方', icon: 'notes-o', bg: '#F0FDFA', iconBg: '#2DD4BF' }
]

const quickItems: ServiceItem[] = [
  { label: '电子健康卡', icon: 'card-o', bg: 'rgba(16, 185, 129, 0.1)', iconBg: 'transparent' },
  { label: '电子票据', icon: 'bill-o', bg: 'rgba(245, 158, 11, 0.1)', iconBg: 'transparent' },
  { label: '就诊人管理', icon: 'friends-o', bg: 'rgba(16, 185, 129, 0.1)', iconBg: 'transparent', path: '/patient' },
  { label: '地址管理', icon: 'location-o', bg: 'rgba(90, 200, 250, 0.1)', iconBg: 'transparent' }
]

const outpatientServices: ServiceItem[] = [
  { label: '预约挂号', icon: 'cluster-o', bg: 'rgba(16, 185, 129, 0.1)', iconBg: 'transparent', path: '/department' },
  { label: '门诊充值', icon: 'balance-o', bg: 'rgba(245, 158, 11, 0.1)', iconBg: 'transparent' },
  { label: '门诊费用', icon: 'bill-o', bg: 'rgba(16, 185, 129, 0.1)', iconBg: 'transparent', path: '/outpatient-fee' },
  { label: '报告查询', icon: 'records', bg: 'rgba(90, 200, 250, 0.1)', iconBg: 'transparent' },
  { label: '线上退款', icon: 'refund-o', bg: 'rgba(139, 92, 246, 0.1)', iconBg: 'transparent' },
  { label: '病历查询', icon: 'description', bg: 'rgba(16, 185, 129, 0.1)', iconBg: 'transparent', path: '/medical-record' },
  { label: '云胶片', icon: 'photograph', bg: 'rgba(90, 200, 250, 0.1)', iconBg: 'transparent' },
  { label: '智能分诊', icon: 'search', bg: 'rgba(245, 158, 11, 0.1)', iconBg: 'transparent' }
]

const inpatientServices: ServiceItem[] = [
  { label: '住院预约', icon: 'hotel-o', bg: 'rgba(16, 185, 129, 0.1)', iconBg: 'transparent' },
  { label: '住院充值', icon: 'balance-o', bg: 'rgba(245, 158, 11, 0.1)', iconBg: 'transparent' },
  { label: '住院费用', icon: 'bill-o', bg: 'rgba(16, 185, 129, 0.1)', iconBg: 'transparent' },
  { label: '陪护管理', icon: 'friends-o', bg: 'rgba(90, 200, 250, 0.1)', iconBg: 'transparent' }
]

function handleSearch() {
  router.push({ path: '/department', query: { keyword: keyword.value } })
}

function handleService(item: ServiceItem) {
  if (item.path) {
    router.push(item.path)
  } else {
    showToast('功能开发中')
  }
}
</script>

<style scoped lang="scss">
.home-page {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.home-header {
  display: flex;
  align-items: center;
  justify-content: space-between;

  .hospital-info {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .hospital-icon {
    font-size: 22px;
    color: $color-primary;
  }

  .hospital-name {
    font-size: $font-size-h3;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 20px;
    color: $color-text-secondary;
  }
}

.search-card {
  :deep(.van-search) {
    padding: 0;
    background: transparent;
  }
}

.banner-card {
  overflow: hidden;
}

.banner-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  background: linear-gradient(135deg, #34D399 0%, #10B981 100%);
  color: #fff;

  .banner-title {
    font-size: 20px;
    font-weight: $font-weight-bold;
    line-height: 1.4;
  }

  .banner-btn {
    display: inline-block;
    margin-top: 12px;
    padding: 6px 14px;
    background: rgba(255, 255, 255, 0.25);
    border-radius: 999px;
    font-size: $font-size-sm;
  }

  .banner-icon {
    font-size: 64px;
    opacity: 0.9;
  }
}

.top-services {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.top-service-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  border-radius: $radius-lg;

  .top-service-icon {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-md;
    color: #fff;
    font-size: 22px;
  }

  .top-service-title {
    font-size: $font-size-h3;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  .top-service-desc {
    margin-top: 4px;
    font-size: $font-size-xs;
    color: $color-text-secondary;
  }
}

.quick-card {
  :deep(.van-grid-item__content) {
    padding: 16px 0;
  }

  .quick-icon-wrap {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-md;
    color: $color-primary;
    font-size: 22px;
    margin-bottom: 8px;
  }

  .quick-text {
    font-size: $font-size-sm;
    color: $color-text-primary;
  }
}

.tab-card {
  overflow: hidden;

  :deep(.van-tabs__nav) {
    background: transparent;
  }

  :deep(.van-tab) {
    font-size: $font-size-body;
  }

  :deep(.van-tabs__line) {
    background: $color-primary;
    width: 24px;
  }

  .tab-grid {
    padding: 8px 0 16px;
  }

  :deep(.van-grid-item__content) {
    padding: 12px 0;
  }

  .tab-icon-wrap {
    width: 44px;
    height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: $radius-md;
    color: $color-primary;
    font-size: 22px;
    margin-bottom: 8px;
  }

  .tab-text {
    font-size: $font-size-sm;
    color: $color-text-primary;
  }
}
</style>
