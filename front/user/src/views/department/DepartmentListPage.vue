<template>
  <div class="page">
    <van-nav-bar title="科室列表" left-arrow @click-left="router.back()" />

    <div class="search-wrap">
      <van-search
        v-model="keyword"
        placeholder="搜索科室"
        shape="round"
        @search="handleSearch"
      />
    </div>

    <div class="dept-layout">
      <!-- 左侧：父科室侧栏（搜索时隐藏，展示全局搜索结果） -->
      <van-sidebar v-if="!hasKeyword" v-model="activeParent" class="sidebar">
        <van-sidebar-item
          v-for="dept in parentList"
          :key="dept.id"
          :title="dept.name"
        />
      </van-sidebar>

      <!-- 右侧：子科室网格 / 搜索结果 -->
      <div class="child-area">
        <van-loading v-if="loading" size="24px" />
        <empty-state v-else-if="!childList.length && !doctorList.length" :description="emptyText" />
        <template v-else-if="hasKeyword">
          <template v-if="childList.length">
            <div class="section-title">科室</div>
            <van-grid :column-num="2" :border="false" :gutter="10">
              <van-grid-item
                v-for="dept in childList"
                :key="dept.id"
                :text="dept.name"
                @click="router.push(`/department/${dept.id}`)"
              >
                <template #icon>
                  <van-icon name="wap-home-o" size="24" color="var(--van-primary-color)" />
                </template>
              </van-grid-item>
            </van-grid>
          </template>
          <template v-if="doctorList.length">
            <div class="section-title">医生</div>
            <div class="doctor-list">
              <div
                v-for="d in doctorList"
                :key="d.doctorId"
                class="doctor-item surface-card"
                @click="router.push(`/doctor/${d.doctorId}`)"
              >
                <van-image v-if="d.avatar" round width="44" height="44" :src="d.avatar" />
                <van-icon v-else name="manager" size="44" color="#c8c9cc" />
                <div class="doctor-info">
                  <div class="doctor-name">{{ d.doctorName }} · {{ d.positionName }}</div>
                  <div class="doctor-dept">{{ d.departmentName }}</div>
                </div>
                <van-icon name="arrow" color="#c8c9cc" />
              </div>
            </div>
          </template>
        </template>
        <van-grid v-else :column-num="2" :border="false" :gutter="10">
          <van-grid-item
            v-for="dept in childList"
            :key="dept.id"
            :text="dept.name"
            @click="router.push(`/department/${dept.id}`)"
          >
            <template #icon>
              <van-icon name="wap-home-o" size="24" color="var(--van-primary-color)" />
            </template>
          </van-grid-item>
        </van-grid>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import EmptyState from '@/components/EmptyState.vue'
import { getParentBaseInfoList, getChildBaseInfoList } from '@/api/department'
import type { ParentDepartmentBaseInfo, ChildDepartmentBaseInfo } from '@/api/department'
import { searchDoctorsByName } from '@/api/doctor'
import type { DoctorSearchItem } from '@/api/doctor'
import { showToast } from 'vant'

/**
 * 科室列表页
 * @Author: YiRanCrazy@gmail.com
 * @Description: 左侧父科室侧栏 + 右侧子科室网格
 * @Datetime: 2026-07-18 14:00
 * @Version: 1.0
 */

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const activeParent = ref(0)
const keyword = ref('')

const parentList = ref<ParentDepartmentBaseInfo[]>([])
const allChildren = ref<ChildDepartmentBaseInfo[]>([])
const doctorList = ref<DoctorSearchItem[]>([])

const hasKeyword = computed(() => keyword.value.trim().length > 0)

/** 当前选中父科室的子科室，或搜索过滤 */
const childList = computed(() => {
  const k = keyword.value.trim()
  if (k) {
    return allChildren.value.filter((c) => c.name.toLowerCase().includes(k.toLowerCase()))
  }
  const parent = parentList.value[activeParent.value]
  if (!parent) return []
  return allChildren.value.filter((c) => c.parentId === parent.id)
})

const emptyText = computed(() => (hasKeyword.value ? '暂无搜索结果' : '暂无科室'))

function handleSearch() {
  router.replace({ query: { keyword: keyword.value || undefined } })
}

/** 按姓名模糊搜索医生；关键词为空或请求失败时置空列表 */
async function searchDoctors() {
  const k = keyword.value.trim()
  if (!k) {
    doctorList.value = []
    return
  }
  try {
    const res = await searchDoctorsByName(k)
    doctorList.value = res.data?.list || []
  } catch {
    doctorList.value = []
  }
}

onMounted(async () => {
  keyword.value = String(route.query.keyword || '')
  if (keyword.value.trim()) {
    searchDoctors()
  }
  loading.value = true
  try {
    const [parentRes, childRes] = await Promise.all([
      getParentBaseInfoList(),
      getChildBaseInfoList()
    ])
    parentList.value = parentRes.data || []
    allChildren.value = childRes.data || []
  } catch {
    showToast('加载科室失败')
  } finally {
    loading.value = false
  }
})

// U26: 用户已在科室页时 HomePage 再次 push keyword，onMounted 不重触发，需 watch query 同步
watch(() => route.query.keyword, (k) => {
  keyword.value = typeof k === 'string' ? k : ''
  searchDoctors()
})
</script>

<style scoped lang="scss">
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
  background: $color-bg-page;
}

.search-wrap {
  padding: 8px 12px;
  background: $color-bg-card-solid;

  :deep(.van-search) {
    padding: 0;
  }
}

.dept-layout {
  display: flex;
  flex: 1;
  min-height: 0;
}

.sidebar {
  width: 90px;
  flex-shrink: 0;
  background: $color-bg-page;
  border-right: 1px solid $color-divider;
  overflow-y: auto;
}

.child-area {
  flex: 1;
  min-width: 0;
  padding: 12px;
  overflow-y: auto;
}

.section-title {
  margin: 4px 0 8px;
  font-size: 13px;
  color: $color-text-tertiary;
}

.doctor-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.doctor-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  cursor: pointer;

  .doctor-info {
    flex: 1;
    min-width: 0;
  }

  .doctor-name {
    font-size: 15px;
    font-weight: 600;
    color: $color-text-primary;
  }

  .doctor-dept {
    margin-top: 2px;
    font-size: 12px;
    color: $color-text-tertiary;
  }
}
</style>
