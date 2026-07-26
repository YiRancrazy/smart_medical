import request from './index'
import type { ApiResult } from './types'

/**
 * 就诊人模块 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 就诊人管理、默认就诊人设置
 * @Datetime: 2026-07-17 11:00
 * @Version: 1.0
 */

export interface PatientCardSimpleResponse {
  userId: string
  userPatientRelationId: string
  patientId: string
  patientName: string
  patientIdCard: string
  patientPhone: string
  patientCardSn: string
  patientCardId: string
  relation: string
  defaultPatient: boolean
  remark?: string
}

export interface OutPatientCardBaseInfo {
  patientCardId: string
  patientName: string
  patientPhone: string
  patientIdCard: string
}

export interface RegistrationConfirmPatientCardVo {
  patientCardNo: string
  patientName: string
  patientPhone: string
  patientIdCard: string
  relation: string
  defaultPatientCard: boolean
}

/**
 * 默认就诊人信息
 */
export function getDefaultPatientBaseInfo() {
  return request.get<any, ApiResult<OutPatientCardBaseInfo>>(
    '/api/user/v1/patient/card/baseinfo'
  )
}

/**
 * 所有就诊人（确认页）
 */
export function getConfirmPatientBaseInfo() {
  return request.get<any, ApiResult<RegistrationConfirmPatientCardVo[]>>(
    '/api/user/v1/patient/card/confirm/baseinfo'
  )
}

/**
 * 就诊人列表
 */
export function getPatientSimpleList() {
  return request.get<any, ApiResult<PatientCardSimpleResponse[]>>(
    '/api/user/v1/patient/card/list/simple/response'
  )
}

/**
 * 就诊人详情（F24: 替代拉全列表 find by id，含 remark）
 */
export function getPatientDetail(relationId: number | string) {
  return request.get<any, ApiResult<PatientCardSimpleResponse>>(
    `/api/user/v1/patient/card/detail/${relationId}`
  )
}

/**
 * 添加就诊人
 */
export function addPatient(params: {
  name: string
  idCard: string
  phone: string
  relation: string
  remark: string
  defaulted: string
}) {
  return request.post<any, ApiResult<number>>(
    '/api/user/v1/patient/relation',
    params
  )
}

/**
 * 修改就诊人
 */
export function updatePatient(params: {
  id: number | string
  name: string
  idCard: string
  phone: string
  relation: string
  remark: string
  defaulted: string
}) {
  const { id, ...body } = params
  return request.put<any, ApiResult<number>>(
    `/api/user/v1/patient/relation/${id}`,
    body
  )
}

/**
 * 设置默认就诊人
 */
export function setDefaultPatient(id: number | string) {
  return request.put<any, ApiResult<number>>(
    `/api/user/v1/patient/relation/default/${id}`
  )
}

/**
 * 删除就诊人
 */
export function deletePatient(id: number | string) {
  return request.delete<any, ApiResult<number>>(`/api/user/v1/patient/relation/${id}`)
}