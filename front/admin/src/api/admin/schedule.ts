import request from '../index'
import type { Result, PageInfo } from '../types'

/**
 * 排班模板管理 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 排班模板的分页查询、停诊、启用、删除
 * @Datetime: 2026-07-17 10:15
 * @Version: 1.0
 */

export interface ScheduleTemplateResponse {
  id: string
  doctorId: string
  doctorName: string
  departmentId: string
  departmentName: string
  scheduleDate: string
  scheduleType: string
  startTime: string
  endTime: string
  remaining: string
  total: string
  address: string
  status: string // "0"-停诊, "1"-启用（后端统一返回字符串）
  price: string
  remark: string
}

export interface ScheduleConditionsParams {
  pageNum?: number
  pageSize?: number
  doctorId?: number
  startDate?: string
  endDate?: string
  departmentId?: number
}

/**
 * 排班模板分页查询（路径变量版本）
 */
export function listScheduleTemplatesByPath(pageNum: number, pageSize: number) {
  return request.get<any, Result<PageInfo<ScheduleTemplateResponse>>>(
    `/api/admin/v1/registration/schedule/template/list/page/${pageNum}/${pageSize}`
  )
}

/**
 * 排班模板分页查询（Query 参数版本）
 * 注意：返回 PageInfo（PageHelper 结构）
 */
export function listScheduleTemplates(params: ScheduleConditionsParams) {
  return request.get<any, Result<PageInfo<ScheduleTemplateResponse>>>(
    '/api/admin/v1/registration/schedule/template/list/page',
    { params }
  )
}

/**
 * 停诊
 */
export function stopSchedule(id: string) {
  return request.put<any, Result<number>>(`/api/admin/v1/registration/schedule/template/stop/${id}`)
}

/**
 * 启用
 */
export function startSchedule(id: string) {
  return request.put<any, Result<number>>(`/api/admin/v1/registration/schedule/template/start/${id}`)
}

/**
 * 删除排班模板
 */
export function deleteSchedule(id: string) {
  return request.delete<any, Result<number>>(`/api/admin/v1/registration/schedule/template/delete/${id}`)
}