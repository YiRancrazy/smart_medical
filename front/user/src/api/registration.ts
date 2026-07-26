import request from './index'
import type { ApiResult } from './types'

/**
 * 挂号模块 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 排班查询、预约挂号、报到、取消
 * @Datetime: 2026-07-17 10:50
 * @Version: 1.0
 */

export interface RegistrationDateAndRemainQuotaVo {
  date: string
  remainQuota: number
  totalQuota: number
}

export interface RegistrationConfirmTime {
  registrationScheduleId: string
  startTime: string
  endTime: string
  remainQuota: number
  available: boolean
}

export interface InsertRegistrationRequest {
  paymentMethodId: string
  registrationScheduleId: string
  userId: string
  patientCardId: string
}

// U20: 与后端 AppointmentResponseSimple DTO 对齐，删除后端不返回的 registrationId/registrationStatus/appointmentData/appointmentDate/appointmentStartTime
export interface AppointmentResponseSimple {
  id: string
  orderId: string
  status: number
  scheduleDate: string
  scheduleTime: string
  doctorId: string
  doctorName: string
  doctorAvatar: string
  doctorPosition: string
  departmentId: string
  departmentName: string
  patientName: string
  registrationPrice: number
}

// U27: 后端挂号列表接口返回 PageResult
export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

/**
 * 医生最近7天排班
 */
export function getRecentSchedule(doctorId: number) {
  return request.get<any, ApiResult<RegistrationDateAndRemainQuotaVo[]>>(
    '/api/user/v1/registration/schedule/doctor/recent',
    { params: { doctorId } }
  )
}

/**
 * 医生+日期时段排班
 */
export function getTimeSlots(doctorId: number, date: string) {
  return request.get<any, ApiResult<RegistrationConfirmTime[]>>(
    '/api/user/v1/registration/schedule/time',
    { params: { doctorId, date } }
  )
}

/**
 * 挂号价格
 */
export function getSchedulePrice(registrationScheduleId: number | string) {
  return request.get<any, ApiResult<number>>(
    '/api/user/v1/registration/schedule/price',
    { params: { registrationScheduleId } }
  )
}

/**
 * 提交预约
 */
export function submitRegistration(data: InsertRegistrationRequest) {
  return request.post<any, ApiResult<string>>('/api/user/v1/registration', data)
}

/**
 * 挂号列表（U27: 返回 PageResult）
 */
export function getRegistrationList(patientCardId?: string) {
  return request.get<any, ApiResult<PageResult<AppointmentResponseSimple>>>(
    '/api/user/v1/registration/simple/list',
    { params: patientCardId ? { patientCardId } : undefined }
  )
}

/**
 * 挂号详情
 */
export function getRegistrationDetail(id: number | string) {
  return request.get<any, ApiResult<any>>(`/api/user/v1/registration/${id}`)
}

/**
 * 挂号报到
 */
export function checkIn(registrationId: number | string) {
  return request.post<any, ApiResult<void>>(
    `/api/user/v1/registration/${registrationId}/check-in`
  )
}

/**
 * 取消预约
 */
export function cancelRegistration(registrationId: number | string, reason?: string) {
  return request.post<any, ApiResult<void>>(
    `/api/user/v1/registration/${registrationId}/cancel`,
    null,
    { params: { reason } }
  )
}