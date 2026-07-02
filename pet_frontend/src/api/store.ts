import http from "./index"
import type { Store } from "../types/store"

export interface NearbyStore {
  id: string
  storeName: string
  storePhone?: string
  storeDesc?: string
  address?: string
  province?: string
  city?: string
  district?: string
  longitude: number
  latitude: number
  distance?: number
  storeLogo?: string
  productCount?: number
}

export interface NearbyQuery {
  latitude: number
  longitude: number
  radius?: number
}

/** 附近门店搜索 */
export async function searchNearby(query: NearbyQuery): Promise<NearbyStore[]> {
  const res = await http.get('/api/store/nearby', { params: query })
  return (res as any).data || res || []
}

/** 门店详情 */
export async function getStoreDetail(id: string): Promise<NearbyStore> {
  const res = await http.get('/api/store/' + id)
  return (res as any).data || res
}

/** 门店商品 */
export async function getStoreProducts(id: string): Promise<any[]> {
  const res = await http.get('/api/store/' + id + '/products')
  return (res as any).data || res || []
}
