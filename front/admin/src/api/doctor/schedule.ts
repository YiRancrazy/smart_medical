import request from '../index'
import type { Result } from '../types'

/**
 * 医生今日排班 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生查看今日排班详情
 * @Datetime: 2026-07-17 10:22
 * @Version: 1.0
 */

/** 后端 DoctorScheduleVO 字段 */
export interface DoctorScheduleVO {
  registrationId: string
  status: number
  shiftName: string
  startTime: string
  endTime: string
  patientId: string
  patientName: string
  patientPhone: string
  registrationTime: string
}

/**
 * 获取医生今日排班（doctorId 由后端 JWT 自动注入）
 */
export function getTodaySchedule() {
  return request.get<any, Result<DoctorScheduleVO[]>>('/api/doctor/v1/schedule/today')
}