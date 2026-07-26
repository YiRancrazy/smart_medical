import request from './index'
import type { ApiResult } from './types'

/**
 * 医生模块 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生详情、挂号相关信息
 * @Datetime: 2026-07-17 10:45
 * @Version: 1.0
 */

export interface DoctorVo {
  doctorId: string
  doctorName: string
  departmentId: number
  departmentName: string
  avatar: string
  doctorPositionId: string
  positionName: string
  degreeId: string
  degreeName: string
  description: string
  tags: string
  status: number
}

export interface RegistrationDoctorBaseInfo {
  doctorId: string
  doctorName: string
  departmentName: string
  position: string
  avatar: string
  price: number
  score: number
  consultationCount: number
  tags: string[]
}

export interface RegistrationDoctorConfirmVo {
  id: string
  name: string
  departmentName: string
  avatar: string
  positionName: string
}

/**
 * 医生详情
 */
export function getDoctorById(id: number) {
  return request.get<any, ApiResult<DoctorVo>>(`/api/user/v1/doctor/${id}`)
}

/**
 * 科室下医生挂号信息
 */
export function getDoctorRegistrationBaseInfo(departmentId: number) {
  return request.get<any, ApiResult<RegistrationDoctorBaseInfo[]>>(
    '/api/user/v1/doctor/registration/baseInfo',
    { params: { departmentId } }
  )
}

/**
 * 医生挂号确认信息
 */
export function getDoctorRegistrationConfirm(doctorId: number | string) {
  return request.get<any, ApiResult<RegistrationDoctorConfirmVo>>(
    '/api/user/v1/doctor/registration/confirm',
    { params: { doctorId } }
  )
}