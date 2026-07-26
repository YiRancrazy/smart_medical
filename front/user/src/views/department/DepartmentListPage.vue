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
      <!-- 左侧：父科室侧栏 -->
      <van-sidebar v-model="activeParent" class="sidebar">
        <van-sidebar-item
          v-for="dept in parentList"
          :key="dept.id"
          :title="dept.name"
        />
      </van-sidebar>

      <!-- 右侧：子科室网格 -->
      <div class="child-area">
        <van-loading v-if="loading" size="24px" />
        <empty-state v-else-if="!childList.length" :description="emptyText" />
        <van-grid v-else :column-num="2" :border="false" :gutter="10">
          <van-grid-item
            v-for="dept in childList"
            :key="dept.id"
            :text="dept.name"
            @click="router.push(`/department/${dept.id}`)"
          >
            <template #icon>
              <van-icon name="wap-home-o" size="24" color="#1989fa" />
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

onMounted(async () => {
  keyword.value = String(route.query.keyword || '')
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
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.search-wrap {
  padding: 8px 12px;
  background: #fff;

  :deep(.van-search) {
    padding: 0;
  }
}

.dept-layout {
  display: flex;
  height: calc(100vh - 46px - 58px);
}

.sidebar {
  width: 90px;
  flex-shrink: 0;
  background: #f7f8fa;
  overflow-y: auto;
}

.child-area {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
}
</style>
