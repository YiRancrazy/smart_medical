import request from '../index'
import type { Result, PageResult } from '../types'

/**
 * 医生管理 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 医生的查询、分页
 * @Datetime: 2026-07-17 10:12
 * @Version: 1.0
 */

export interface DoctorSimpleResponse {
  id: number
  name: string
  departmentId: number
  departmentName: string
  position: string
}

/** 后端 AdminDoctorDetailResponse 字段 */
export interface DoctorDetailResponse {
  doctorId: string
  doctorName: string
  departmentId: string
  departmentName: string
  positionId: string
  positionName: string
  degreeId: string
  degreeName: string
  avatar: string
  address: string
  scope: string
  tags: string[] | string
  description: string
  status: string
}

export interface DoctorConditionsParams {
  username?: string
  departmentId?: number
  current?: number
  size?: number
}

/**
 * 医生姓名模糊查询
 */
export function listDoctorsSimple(name: string) {
  return request.get<any, Result<DoctorSimpleResponse[]>>('/api/admin/v1/doctor/simple', { params: { name } })
}

/**
 * 医生多条件分页查询
 */
export function listDoctorsDetail(params: DoctorConditionsParams) {
  return request.get<any, Result<PageResult<DoctorDetailResponse>>>('/api/admin/v1/doctor/detail/list', { params })
}

/**
 * 医生详情
 */
export function getDoctorById(id: number) {
  return request.get<any, Result<DoctorDetailResponse>>(`/api/admin/v1/doctor/${id}`)
}

/**
 * 新增医生（同时创建登录账户，初始密码 = 姓名拼音首字母大写 + 手机号）
 */
export function createDoctor(data: Partial<DoctorDetailResponse> & { name: string; phone: string; positionId: string | number; degreeId: string | number; email?: string }) {
  return request.post<any, Result<number>>('/api/admin/v1/doctor/', data)
}

/**
 * 查询全部职称
 */
export function listDoctorPositions() {
  return request.get<any, Result<{ id: number; name: string }[]>>('/api/admin/v1/doctor/positions')
}

/**
 * 查询全部学历
 */
export function listDegrees() {
  return request.get<any, Result<{ id: number; name: string }[]>>('/api/admin/v1/doctor/degrees')
}

/**
 * 上传医生头像
 */
export function uploadDoctorAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, Result<string>>('/api/admin/v1/doctor/upload/image', formData)
}

/**
 * 修改医生
 */
export function updateDoctor(id: string | number, data: Partial<DoctorDetailResponse> & { name?: string }) {
  return request.put<any, Result<number>>(`/api/admin/v1/doctor/${id}`, data)
}

/**
 * 删除医生
 */
export function deleteDoctor(id: string | number) {
  return request.delete<any, Result<number>>(`/api/admin/v1/doctor/${id}`)
}
