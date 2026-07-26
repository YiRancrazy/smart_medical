export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  pageNum: number
  pageSize: number
  total: number
  totalPages: number
  list: T[]
}

export interface PageInfo<T> {
  pageNum: number
  pageSize: number
  total: number
  pages: number
  list: T[]
}

export interface AuthErrorResponse {
  success: false
  msg: string
}

export interface PageParams {
  pageNum?: number
  pageSize?: number
  current?: number
  size?: number
}
