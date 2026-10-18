import request from '../index'
import type { Result } from '../types'

/**
 * 医生挂号/患者管理 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 候诊列表、就诊中列表、叫号接诊
 * @Datetime: 2026-07-17 10:24
 * @Version: 1.0
 */

/** 后端 WaitingPatientVO 字段 */
export interface WaitingPatientVO {
  registrationId: string
  patientId: string
  patientName: string
  patientPhone: string
  status: number
  checkInTime: string
  registrationTime: string
}

/**
 * 获取候诊列表
 */
export function getWaitingList() {
  return request.get<any, Result<WaitingPatientVO[]>>('/api/doctor/v1/registration/waiting')
}

/**
 * 获取就诊中列表
 * 注意：后端该接口未按医生过滤，返回所有就诊中挂号
 */
export function getInProgressList() {
  return request.get<any, Result<WaitingPatientVO[]>>('/api/doctor/v1/registration/in-progress')
}

/**
 * 叫号
 */
export function callPatient(registrationId: number | string) {
  return request.post<any, Result<void>>(`/api/doctor/v1/registration/${registrationId}/call`)
}