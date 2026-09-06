import request from '../index'
import type { Result } from '../types'

/**
 * 医生排班 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生月度排班视图查询
 * @Datetime: 2026-07-17 10:22
 * @Version: 1.0
 */

/** 后端 DoctorScheduleViewVO 字段（纯排班信息，无患者数据） */
export interface DoctorScheduleViewVO {
  scheduleId: string
  scheduleDate: string
  shiftName: string
  startTime: string
  endTime: string
  location: string
  remark: string
  status: number
}

/**
 * 获取医生月度排班（日 / 周 / 月视图共用数据源）
 * @param month 月份，格式 yyyy-MM
 */
export function getMonthSchedule(month: string) {
  return request.get<any, Result<DoctorScheduleViewVO[]>>('/api/doctor/v1/schedule/month', {
    params: { month }
  })
}
