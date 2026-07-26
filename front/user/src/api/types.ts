export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface AuthErrorResponse {
  success: false
  msg: string
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

export interface LoginVo {
  accountId: string
  token: string
  uid: string
  phone: string
  userName: string
}
