import request from './index'
import type { ApiResult, PageInfo } from './types'

/**
 * 科室模块 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 科室查询、分页、BaseInfo列表
 * @Datetime: 2026-07-17 10:40
 * @Version: 1.0
 */

export interface Department {
  id: number
  name: string
  sn: string
  type: number
  status: number
  parentId: number | null
  createTime: string
  updateTime: string
}

export interface ParentDepartmentBaseInfo {
  id: number
  name: string
  sn: string
  type: number
  status: number
}

export interface ChildDepartmentBaseInfo {
  id: number
  name: string
  sn: string
  type: number
  status: number
  parentId: number
}

/**
 * 科室详情
 */
export function getDepartmentById(id: number) {
  return request.get<any, ApiResult<Department>>(`/api/user/v1/department/${id}`)
}

/**
 * 科室全量列表
 */
export function getDepartmentList() {
  return request.get<any, ApiResult<Department[]>>('/api/user/v1/department/list')
}

/**
 * 科室分页
 */
export function getDepartmentPage(params: { pageNum: number; pageSize: number }) {
  return request.get<any, ApiResult<PageInfo<Department>>>('/api/user/v1/department/page', { params })
}

/**
 * 一级科室列表
 */
export function getParentList() {
  return request.get<any, ApiResult<Department[]>>('/api/user/v1/department/parent/list')
}

/**
 * 二级科室列表
 */
export function getChildList() {
  return request.get<any, ApiResult<Department[]>>('/api/user/v1/department/child/list')
}

/**
 * 按 parentId 取二级科室
 */
export function getChildListByParent(parentId: number) {
  return request.get<any, ApiResult<Department[]>>(
    '/api/user/v1/department/child/list/parentId',
    { params: { parentId } }
  )
}

/**
 * 一级科室 BaseInfo
 */
export function getParentBaseInfoList() {
  return request.get<any, ApiResult<ParentDepartmentBaseInfo[]>>(
    '/api/user/v1/department/parent/baseInfo/list'
  )
}

/**
 * 二级科室 BaseInfo
 */
export function getChildBaseInfoList() {
  return request.get<any, ApiResult<ChildDepartmentBaseInfo[]>>(
    '/api/user/v1/department/child/baseInfo/list'
  )
}