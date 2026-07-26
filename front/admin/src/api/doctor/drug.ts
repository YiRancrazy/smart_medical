import request from '../index'
import type { Result } from '../types'

/**
 * 医生端药品 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 病历开方时按名称搜索药品
 * @Datetime: 2026-07-22 10:30
 * @Version: 1.0
 */

export interface DrugVO {
  id: number
  drugCode: string
  commonName: string
  tradeName: string
  specification: string
  unit: string
  price: number
}

/**
 * 按名称搜索药品
 */
export function searchDrugs(keyword: string) {
  return request.get<any, Result<DrugVO[]>>(
    `/api/doctor/v1/drug/search`,
    { params: { keyword } }
  )
}
