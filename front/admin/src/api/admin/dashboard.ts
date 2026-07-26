import request from '@/api/index'
import type { Result } from '@/api/types'

/**
 * 工作台统计 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 管理员工作台统计数据
 * @Datetime: 2026-07-24 16:00
 * @Version: 1.0
 */

export interface DashboardStats {
  todayRegistrationCount: number
  waitingVisitCount: number
  inTreatmentCount: number
  pendingDispenseCount: number
  pendingRefundCount: number
  inventoryAlertCount: number
  newPrescriptionCount: number
}

export function getDashboardStats() {
  return request.get<any, Result<DashboardStats>>('/api/admin/v1/dashboard/stats')
}
