export interface Result<T> { code: number; message: string; data: T }
export interface PageResult<T> {
  records: T[]
  total: number
  page?: number
  current?: number
  size: number
}
export interface PageParam { page?: number; size?: number; [key: string]: unknown }
