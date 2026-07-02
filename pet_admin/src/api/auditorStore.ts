import { auditorHttp } from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Store } from "../types/store"

export function listAuditorStores(params: { current: number; size: number; keyword?: string; status?: number }) {
  return unwrap<PageResult<Store>>(auditorHttp.get("/store/search", { params }))
}

export function getAuditorStore(id: string) {
  return unwrap<Store>(auditorHttp.get(`/store/${id}`))
}

function auditStore(id: string, action: "approve" | "reject" | "close", reason?: string) {
  return unwrap<Store>(auditorHttp.put(`/store/${id}/${action}`, null, { params: reason ? { reason } : undefined }))
}

export const approveStore = (id: string, reason?: string) => auditStore(id, "approve", reason)
export const rejectStore = (id: string, reason?: string) => auditStore(id, "reject", reason)
export const closeStore = (id: string, reason?: string) => auditStore(id, "close", reason)
