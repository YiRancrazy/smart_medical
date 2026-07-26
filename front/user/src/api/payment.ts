import request from './index'
import type { ApiResult } from './types'

/**
 * 支付模块 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 支付记录、支付方式
 * @Datetime: 2026-07-17 10:55
 * @Version: 1.0
 */

export interface PaymentRecordSimpleResponse {
  paymentRecordId: string
  paymentTime: string
  orderId: string
  orderType: string
  orderItems: string[]
  paymentMethodName: string
  paymentStatus: string
  paymentAmount: string
}

export interface PaymentMethod {
  id: number
  name: string
  icon: string
  enabled: boolean
}

export interface PayMethodVo {
  id: string
  name: string
  icon: string
}

/**
 * 支付记录列表
 */
export function getPaymentRecordList() {
  return request.get<any, ApiResult<PaymentRecordSimpleResponse[]>>(
    '/api/user/v1/payment/record/simple/list'
  )
}

/**
 * 模拟支付
 */
export function pay(params: {
  orderId: number | string
  paymentMethodId?: number
  transactionSn?: number
  /** 实付金额，单位：分（整数）。后端为 Integer，传浮点会转换失败返回 400 */
  realAmount?: number
}) {
  // U29: realAmount 后端为 Integer（分），Math.round 取整避免浮点导致 400
  const payload = {
    ...params,
    realAmount: params.realAmount != null ? Math.round(params.realAmount) : undefined
  }
  return request.post<any, ApiResult<void>>('/api/user/v1/payment/record/pay', null, { params: payload })
}

/**
 * 支付方式列表
 */
export function getAllPaymentMethods() {
  return request.get<any, ApiResult<PaymentMethod[]>>('/api/user/v1/payment/method/all')
}

/**
 * 默认支付方式
 */
export function getDefaultPaymentMethod() {
  return request.get<any, ApiResult<PayMethodVo>>('/api/user/v1/payment/method/default')
}