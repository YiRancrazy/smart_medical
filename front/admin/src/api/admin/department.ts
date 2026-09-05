import request from '../index'
import type { Result, PageResult } from '../types'

/**
 * 科室管理 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 科室的增删改查、树形展示、分页查询
 * @Datetime: 2026-07-17 10:08
 * @Version: 1.0
 */

/** 后端 AdminDepartmentSimpleResponse 字段 */
export interface DepartmentSimple {
  id: string
  sn: string
  name: string
  type: string
  parentDepartmentId: string
  parentDepartmentName: string
  description: string
  managerId: string
  managerName: string
  managerPhone: string
  phone: string
  address: string
  status: string
}

export interface DepartmentConditionsParams {
  name?: string
  sn?: string
  type?: number
  status?: number
  parentId?: number
  current?: number
  size?: number
}

/**
 * 科室全量列表
 */
export function listDepartments() {
  return request.get<any, Result<DepartmentSimple[]>>('/api/admin/v1/department/list')
}

/**
 * 科室分页（多条件）
 */
export function listDepartmentsByConditions(params: DepartmentConditionsParams) {
  return request.get<any, Result<PageResult<DepartmentSimple>>>('/api/admin/v1/department/list/conditions', { params })
}

/**
 * 科室树
 * 返回 Map<DepartmentSimple, DepartmentSimple[]>
 */
export function getDepartmentTree() {
  return request.get<any, Result<Map<DepartmentSimple, DepartmentSimple[]>>>('/api/admin/v1/department/list/tree')
}

/**
 * 一级科室分页
 */
export function listParentDepartments(current: number, size: number) {
  return request.get<any, Result<PageResult<DepartmentSimple>>>(
    `/api/admin/v1/department/list/parent/${current}/${size}`
  )
}

/**
 * 二级科室分页
 */
export function listChildDepartments(current: number, size: number) {
  return request.get<any, Result<PageResult<DepartmentSimple>>>(
    `/api/admin/v1/department/list/child/${current}/${size}`
  )
}

/**
 * 新增科室
 */
export function createDepartment(data: Partial<DepartmentSimple>) {
  return request.post<any, Result<number>>('/api/admin/v1/department/', data as any)
}

/**
 * 修改科室
 */
export function updateDepartment(id: string | number, data: Partial<DepartmentSimple>) {
  return request.put<any, Result<number>>(`/api/admin/v1/department/${id}`, data as any)
}

/**
 * 删除科室
 */
export function deleteDepartment(id: string | number) {
  return request.delete<any, Result<number>>(`/api/admin/v1/department/${id}`)
}