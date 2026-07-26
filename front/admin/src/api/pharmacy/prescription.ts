import request from '../index'
import type { Result } from '../types'

/**
 * 药师处方管理 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 待发药列表、处方详情、扫码发药
 * @Datetime: 2026-07-17 10:32
 * @Version: 1.0
 */

export interface PendingPrescriptionVO {
  prescriptionId: number
  orderId: number
  patientId: number
  registrationSn: number
  totalAmount: number
  createdAt: string
}

export interface DispenseVO {
  prescriptionId: number
  prescriptionStatus: number
  dispensedAt: string
  items: DispenseItemVO[]
}

export interface DispenseItemVO {
  drugId: number
  drugName: string
  quantity: number
  stockAfter: number
}

/**
 * 获取待发药列表
 */
export function getPendingList() {
  return request.get<any, Result<PendingPrescriptionVO[]>>('/api/pharmacy/v1/prescription/pending')
}

/**
 * 获取处方详情
 * 注意：返回原始 Prescription 实体
 */
export function getPrescription(id: string | number) {
  return request.get<any, Result<any>>(`/api/pharmacy/v1/prescription/${id}`)
}

/**
 * 发药
 */
export function dispense(prescriptionId: string | number) {
  return request.post<any, Result<DispenseVO>>(`/api/pharmacy/v1/prescription/${prescriptionId}/dispense`)
}
