import request from './index'
import type { ApiResult } from './types'

/**
 * 处方模块 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 处方详情查看
 * @Datetime: 2026-07-17 11:08
 * @Version: 1.0
 */

export interface PrescriptionListVO {
  id: string
  medicalRecordId: string
  totalAmount: number
  status: number
  itemCount: number
  createTime: string
}

export interface PrescriptionDetailVO {
  id: string
  medicalRecordId: string
  patientName: string
  doctorName: string
  departmentName: string
  totalAmount: number
  status: number
  createTime: string
  items: PrescriptionItemDetailVO[]
}

export interface PrescriptionItemDetailVO {
  drugId: string
  drugName: string
  quantity: number
  unitPrice: number
  usageMethod: string
}

/**
 * 我的处方列表
 */
export function getPrescriptionList(patientCardId?: string) {
  return request.get<any, ApiResult<PrescriptionListVO[]>>(
    '/api/user/v1/prescription/list',
    { params: patientCardId ? { patientCardId } : undefined }
  )
}

/**
 * 处方详情
 */
export function getPrescriptionDetail(id: string) {
  return request.get<any, ApiResult<PrescriptionDetailVO>>(`/api/user/v1/prescription/${id}`)
}