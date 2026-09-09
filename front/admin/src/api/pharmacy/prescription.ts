import request from '../index'
import type { Result, PageResult } from '../types'

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

export interface DispenseHistoryQueryParams {
  patientName?: string
  dispenserPhone?: string
  prescriptionId?: string | number
  orderId?: string | number
  startDate?: string
  endDate?: string
  pageNum?: number
  pageSize?: number
}

export interface DispenseHistoryVO {
  prescriptionId: number
  orderId: number
  medicalRecordId: number
  patientName: string
  doctorName: string
  dispenserPhone: string
  totalAmount: number
  itemCount: number
  dispensedAt: string
}

/**
 * 获取待发药列表
 * 返回分页结构，列表在 data.list
 */
export function getPendingList() {
  return request.get<any, Result<PageResult<PendingPrescriptionVO>>>('/api/pharmacy/v1/prescription/pending')
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

/**
 * 发药历史分页列表（已发药处方，按发药时间倒序）
 */
export function pageDispenseHistory(params: DispenseHistoryQueryParams) {
  return request.post<any, Result<PageResult<DispenseHistoryVO>>>(
    '/api/pharmacy/v1/prescription/dispense-history/page',
    params
  )
}
