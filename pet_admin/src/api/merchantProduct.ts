import { merchantHttp } from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Product } from "../types/product"

export interface MerchantProductPayload {
  storeId: string
  productName: string
  productType: number
  category?: string
  productDesc?: string
  price: number
  stock: number
  mainImage?: string
  images?: string
  status?: "0" | "1"
}

export function listMerchantProducts(params: { page: number; size: number; keyword?: string; storeId?: string; status?: number }) {
  return unwrap<PageResult<Product>>(merchantHttp.get("/product/search", { params }))
}

export function getMerchantProduct(id: string) {
  return unwrap<Product>(merchantHttp.get(`/product/${id}`))
}

export function createMerchantProduct(data: MerchantProductPayload) {
  return unwrap<Product>(merchantHttp.post("/product", data))
}

export function updateMerchantProduct(id: string, data: MerchantProductPayload) {
  return unwrap<Product>(merchantHttp.put(`/product/${id}`, data))
}

export function deleteMerchantProduct(id: string) {
  return unwrap<boolean>(merchantHttp.delete(`/product/${id}`))
}

export function onlineMerchantProduct(id: string) {
  return unwrap<Product>(merchantHttp.put(`/product/${id}/online`))
}

export function offlineMerchantProduct(id: string) {
  return unwrap<Product>(merchantHttp.put(`/product/${id}/offline`))
}
