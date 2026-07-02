import { auditorHttp } from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Product } from "../types/product"

export function listAuditorProducts(params: { page: number; size: number; keyword?: string; storeId?: string; status?: number }) {
  return unwrap<PageResult<Product>>(auditorHttp.get("/product/search", { params }))
}

export function getAuditorProduct(id: string) {
  return unwrap<Product>(auditorHttp.get(`/product/${id}`))
}

export function forceOfflineProduct(id: string, reason?: string) {
  return unwrap<Product>(auditorHttp.put(`/product/${id}/force-offline`, null, { params: reason ? { reason } : undefined }))
}

export function releaseOfflineProduct(id: string) {
  return unwrap<Product>(auditorHttp.put(`/product/${id}/release-offline`))
}
