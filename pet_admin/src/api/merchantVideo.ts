import { merchantHttp } from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Video } from "../types/video"

export interface MerchantVideoPayload {
  title: string
  description?: string
  url: string
  cover?: string
  productId?: string
  duration: number
}

export function listMerchantVideos(params: { page: number; size: number; keyword?: string; status?: number }) {
  return unwrap<PageResult<Video>>(merchantHttp.get("/video/search", { params }))
}

export function getMerchantVideo(id: string) {
  return unwrap<Video>(merchantHttp.get("/video/" + id))
}

export function createMerchantVideo(data: MerchantVideoPayload) {
  return unwrap<Video>(merchantHttp.post("/video", data))
}

export function updateMerchantVideo(id: string, data: Partial<MerchantVideoPayload>) {
  return unwrap<Video>(merchantHttp.put("/video/" + id, data))
}

export function deleteMerchantVideo(id: string) {
  return unwrap<boolean>(merchantHttp.delete("/video/" + id))
}

export function onlineMerchantVideo(id: string) {
  return unwrap<Video>(merchantHttp.put("/video/" + id + "/online"))
}

export function offlineMerchantVideo(id: string) {
  return unwrap<Video>(merchantHttp.put("/video/" + id + "/offline"))
}
