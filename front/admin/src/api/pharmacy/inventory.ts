import request from '../index'
import type { Result } from '../types'

/**
 * 药师库存预警 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 查看低于最低库存量的药品列表
 * @Datetime: 2026-07-17 10:35
 * @Version: 1.0
 */

export interface DrugInventory {
  id: number
  drugId: number
  drugName: string
  currentStock: number
  minStock: number
  shortage: number
  updateTime: string
}

/**
 * 获取库存预警列表
 */
export function getLowStockList() {
  return request.get<any, Result<DrugInventory[]>>('/api/pharmacy/v1/inventory/low-stock')
}