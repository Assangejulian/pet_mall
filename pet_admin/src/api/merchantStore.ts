import { merchantHttp } from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Store } from "../types/store"

export interface MerchantStorePayload {
  storeName: string
  storeLogo?: string
  storePhone?: string
  storeDesc?: string
  province?: string
  city?: string
  district?: string
  address: string
  longitude?: number
  latitude?: number
}

export function listMerchantStores(params: { current: number; size: number; keyword?: string; status?: number }) {
  return unwrap<PageResult<Store>>(merchantHttp.get("/store/search", { params }))
}

export function getMerchantStore(id: string) {
  return unwrap<Store>(merchantHttp.get(`/store/${id}`))
}

export function createMerchantStore(data: MerchantStorePayload) {
  return unwrap<boolean>(merchantHttp.post("/store", data))
}

export function updateMerchantStore(id: string, data: MerchantStorePayload) {
  return unwrap<boolean>(merchantHttp.put(`/store/${id}`, data))
}

export function deleteMerchantStore(id: string) {
  return unwrap<boolean>(merchantHttp.delete(`/store/${id}`))
}
