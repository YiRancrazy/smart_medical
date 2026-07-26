import request from '../index'
import type { Result } from '../types'

/**
 * 医生病历管理 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 病历草稿保存、提交（含开处方）
 * @Datetime: 2026-07-17 10:26
 * @Version: 1.0
 */

export interface PatientInfoVO {
  patientId: number
  patientName: string
  patientPhone: string
  patientAge?: number
  patientGender?: number
}

export interface MedicalRecordDetailVO {
  id: number
  registrationId: number
  doctorId: number
  patientId: number
  patientName: string
  patientPhone: string
  chiefComplaint: string
  presentIllness: string
  pastHistory: string
  physicalExam: string
  diagnosis: string
  treatmentPlan: string
  status: number // 0-草稿, 1-已提交
}

export interface DraftMedicalRecordRequest {
  registrationId: number | string
  chiefComplaint: string
  presentIllness: string
  pastHistory: string
  physicalExam: string
  diagnosis: string
  treatmentPlan: string
}

export interface PrescriptionItemRequest {
  drugId: number
  quantity: number
  usageMethod: string
  drugSelectValue?: number | undefined
  drugOptions?: { label: string; value: number }[]
  drugLoading?: boolean
}

export interface SubmitPrescriptionRequest extends DraftMedicalRecordRequest {
  items: PrescriptionItemRequest[]
}

export interface PrescriptionSubmitVO {
  medicalRecordId: number
  prescriptionId: number
  orderId: number
  orderSn: string
  totalAmount: number
  registrationStatus: number
}

/**
 * 获取病历详情
 */
export function getMedicalRecord(registrationId: number | string) {
  return request.get<any, Result<MedicalRecordDetailVO>>(
    `/api/doctor/v1/medical-record/registration/${registrationId}`
  )
}

/**
 * 保存病历草稿
 */
export function saveDraft(data: DraftMedicalRecordRequest) {
  return request.post<any, Result<void>>('/api/doctor/v1/medical-record/draft', data)
}

/**
 * 提交病历+开处方
 */
export function submitMedicalRecord(data: SubmitPrescriptionRequest) {
  return request.post<any, Result<PrescriptionSubmitVO>>('/api/doctor/v1/medical-record/submit', data)
}