import request from './index'
import type { ApiResult, PageResult } from './types'

/**
 * 门诊费用 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 用户端门诊费用列表
 * @Datetime: 2026-07-25 14:00
 * @Version: 1.0
 */

export interface OutpatientFeeItem {
  orderId: string
  orderSn: string
  orderTypeId: string
  orderTypeName: string
  status: number
  statusName: string
  totalAmount: number
  createTime: string
}

/**
 * 查询当前用户的门诊费用列表
 */
export function getOutpatientFeeList(params: {
  current?: number
  size?: number
}) {
  return request.get<any, ApiResult<PageResult<OutpatientFeeItem>>>(
    '/api/user/v1/outpatient-fee/list',
    { params }
  )
}
