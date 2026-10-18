import request from '../index'
import type { Result } from '../types'

/**
 * 文件管理 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 文件模板信息获取
 * @Datetime: 2026-07-18 18:30
 * @Version: 1.0
 */

/** 后端 AdminFileSimpleResponse 字段 */
export interface AdminFileSimpleResponse {
  id: string
  name: string
  md5: string
  path: string
  size: string
}

/**
 * 获取排班模板文件信息
 */
export function getRegistrationTemplate() {
  return request.get<any, Result<AdminFileSimpleResponse>>('/api/admin/v1/file/registration/template')
}
