import request from '../index'
import type { Result, PageInfo } from '../types'

/**
 * 病历历史查询 API（管理端 / 医生端 / 药师端）
 * @Author: YiRanCrazy@gmail.com
 * @Description: 三端病历历史分页列表与详情
 * @Datetime: 2026-07-25 20:30
 * @Version: 1.0
 */

export interface MedicalRecordQueryParams {
  patientName?: string
  startDate?: string
  endDate?: string
  pageNum?: number
  pageSize?: number
}

export interface MedicalRecordPageItemVO {
  id: number
  registrationId: number
  patientName: string
  doctorName: string
  departmentName: string
  diagnosis: string
  visitDate: string
  createTime: string
}

export interface MedicalRecordDetailVO {
  id: number
  registrationId: number
  doctorId: number
  doctorName: string
  departmentName: string
  patientId: number
  patientName: string
  patientPhone: string
  chiefComplaint: string
  presentIllness: string
  pastHistory: string
  physicalExam: string
  diagnosis: string
  treatmentPlan: string
  status: number
}

const ROLE_PREFIX_MAP: Record<string, string> = {
  admin: '/api/admin/v1/medical-record',
  doctor: '/api/doctor/v1/medical-record/history',
  pharmacy: '/api/pharmacy/v1/medical-record'
}

function getPrefix(role: string): string {
  const prefix = ROLE_PREFIX_MAP[role]
  if (!prefix) {
    throw new Error(`不支持的病历历史查询角色: ${role}`)
  }
  return prefix
}

/**
 * 病历历史分页列表
 * @param role admin / doctor / pharmacy
 * @param params 查询参数
 */
export function pageMedicalRecords(role: string, params: MedicalRecordQueryParams) {
  return request.post<any, Result<PageInfo<MedicalRecordPageItemVO>>>(
    `${getPrefix(role)}/page`,
    params
  )
}

/**
 * 病历详情
 * @param role admin / doctor / pharmacy
 * @param id 病历ID
 */
export function getMedicalRecordDetail(role: string, id: string | number) {
  return request.get<any, Result<MedicalRecordDetailVO>>(
    `${getPrefix(role)}/${id}`
  )
}
