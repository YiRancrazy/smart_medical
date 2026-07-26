import request from './index'
import type { ApiResult } from './types'

/**
 * 病历模块 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 病历列表、详情查看
 * @Datetime: 2026-07-17 11:05
 * @Version: 1.0
 */

export interface MedicalRecordListVO {
  id: string
  registrationId: string
  patientName: string
  departmentName: string
  doctorName: string
  diagnosis: string
  visitDate: string
  prescriptionId: string | null
}

export interface MedicalRecord {
  id: string
  registrationId: string
  patientName: string
  patientPhone: string
  chiefComplaint: string
  presentIllness: string
  pastHistory: string
  physicalExam: string
  diagnosis: string
  treatmentPlan: string
  status: number
  prescriptionId: string | null
  createTime: string
  updateTime: string
}

/**
 * 我的病历列表
 */
export function getMedicalRecordList(patientCardId?: string) {
  return request.get<any, ApiResult<MedicalRecordListVO[]>>(
    '/api/user/v1/medical-record/list',
    { params: patientCardId ? { patientCardId } : undefined }
  )
}

/**
 * 病历详情
 */
export function getMedicalRecordDetail(id: string) {
  return request.get<any, ApiResult<MedicalRecord>>(`/api/user/v1/medical-record/${id}`)
}