import request from '../index'
import type { Result, PageInfo } from '../types'

/**
 * 处方历史查询 API（管理端 / 医生端 / 药师端）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 三端处方历史分页列表与详情
 * @Datetime: 2026-07-25 20:32
 * @Version: 1.0
 */

export interface PrescriptionQueryParams {
  patientName?: string
  status?: number
  startDate?: string
  endDate?: string
  pageNum?: number
  pageSize?: number
}

export interface PrescriptionPageItemVO {
  id: number
  medicalRecordId: number
  patientName: string
  doctorName: string
  totalAmount: number
  status: number
  itemCount: number
  createTime: string
}

export interface PrescriptionItemVO {
  drugId: number
  commonName: string
  specification: string
  unit: string
  unitPrice: number
  quantity: number
  usageMethod: string
}

export interface PrescriptionDetailVO {
  id: number
  medicalRecordId: number
  patientId: number
  patientName: string
  patientPhone: string
  doctorName: string
  status: number
  totalAmount: number
  orderId: number
  createTime: string
  items: PrescriptionItemVO[]
}

const ROLE_PAGE_PREFIX_MAP: Record<string, string> = {
  admin: '/api/admin/v1/prescription',
  doctor: '/api/doctor/v1/prescription/history',
  pharmacy: '/api/pharmacy/v1/prescription'
}

const ROLE_DETAIL_PREFIX_MAP: Record<string, string> = {
  admin: '/api/admin/v1/prescription',
  doctor: '/api/doctor/v1/prescription/history',
  pharmacy: '/api/pharmacy/v1/prescription/history'
}

function getPagePrefix(role: string): string {
  const prefix = ROLE_PAGE_PREFIX_MAP[role]
  if (!prefix) {
    throw new Error(`不支持的处方历史查询角色: ${role}`)
  }
  return prefix
}

function getDetailPrefix(role: string): string {
  const prefix = ROLE_DETAIL_PREFIX_MAP[role]
  if (!prefix) {
    throw new Error(`不支持的处方历史查询角色: ${role}`)
  }
  return prefix
}

/**
 * 处方历史分页列表
 * @param role admin / doctor / pharmacy
 * @param params 查询参数
 */
export function pagePrescriptions(role: string, params: PrescriptionQueryParams) {
  return request.post<any, Result<PageInfo<PrescriptionPageItemVO>>>(
    `${getPagePrefix(role)}/page`,
    params
  )
}

/**
 * 处方详情
 * @param role admin / doctor / pharmacy
 * @param id 处方ID
 */
export function getPrescriptionDetail(role: string, id: string | number) {
  return request.get<any, Result<PrescriptionDetailVO>>(
    `${getDetailPrefix(role)}/${id}`
  )
}
