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

    <glass-card class="tab-card" padding="0">
      <div class="section-title">门诊服务</div>
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
    </glass-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import GlassCard from '@/components/GlassCard.vue'

/**
 * 用户首页
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医院首页：搜索、banner、服务入口、门诊服务
 * @Datetime: 2026-07-17 11:50
 * @Version: 1.0
 */

const router = useRouter()
const keyword = ref('')

interface ServiceItem {
  label: string
  icon: string
  bg: string
  path: string
}

const outpatientServices: ServiceItem[] = [
  { label: '预约挂号', icon: 'cluster-o', bg: 'rgba(16, 185, 129, 0.1)', path: '/department' },
  { label: '门诊费用', icon: 'bill-o', bg: 'rgba(16, 185, 129, 0.1)', path: '/outpatient-fee' },
  { label: '处方查询', icon: 'records', bg: 'rgba(90, 200, 250, 0.1)', path: '/prescription' },
  { label: '病历查询', icon: 'description', bg: 'rgba(16, 185, 129, 0.1)', path: '/medical-record' }
]

function handleSearch() {
  router.push({ path: '/department', query: { keyword: keyword.value } })
}

function handleService(item: ServiceItem) {
  router.push(item.path)
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

.tab-card {
  overflow: hidden;

  .section-title {
    padding: 16px 16px 0;
    font-size: $font-size-h3;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
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
