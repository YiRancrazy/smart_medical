import { ref, computed } from 'vue'

/**
 * 分页逻辑封装
 * @Author: YiRanCrazy@gmail.com
 * @Description: 统一处理分页参数、页码变化、总数计算
 * @Datetime: 2026-07-17 11:12
 * @Version: 1.0
 */

export interface PaginationParams {
  pageNum?: number
  pageSize?: number
}

export function usePagination(defaultPageSize = 10) {
  const pageNum = ref(1)
  const pageSize = ref(defaultPageSize)
  const total = ref(0)
  const loading = ref(false)

  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  /**
   * 获取当前分页参数
   */
  function getParams(): PaginationParams {
    return {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }

  /**
   * 处理页码变化
   */
  function handlePageChange(newPage: number) {
    pageNum.value = newPage
  }

  /**
   * 处理每页条数变化
   */
  function handleSizeChange(newSize: number) {
    pageSize.value = newSize
    pageNum.value = 1
  }

  /**
   * 重置分页
   */
  function reset() {
    pageNum.value = 1
    total.value = 0
  }

  return {
    pageNum,
    pageSize,
    total,
    loading,
    totalPages,
    getParams,
    handlePageChange,
    handleSizeChange,
    reset
  }
}