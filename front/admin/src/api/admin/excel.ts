import request from '../index'
import type { Result } from '../types'

/**
 * Excel 上传 API
 * @Author: YiRanCrazy@gmail.com
 * @Description: 排班模板 Excel 上传与下载
 * @Datetime: 2026-07-18 18:30
 * @Version: 1.0
 */

/**
 * 上传排班模板
 */
export function uploadRegistrationTemplate(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  // F21: 不手动设 Content-Type，让浏览器自动生成带 boundary 的 multipart 头
  return request.post<any, Result<number>>(
    '/api/admin/v1/excel/upload/registration/template',
    formData
  )
}

/**
 * 下载排班导入模板
 * 使用 axios 并带 JWT 头，后端接口需要鉴权
 */
export function downloadRegistrationTemplate() {
  return request.get<any, Blob>(
    '/api/admin/v1/excel/download/registration/template',
    { responseType: 'blob' }
  )
}
