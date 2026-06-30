import http from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Video } from "../types/video"
export function listVideos(params: { page: number; size: number; keyword?: string; status?: number }): Promise<PageResult<Video>> { return unwrap(http.get("/video/search", { params })) }
export function updateVideo(id: string, data: Partial<Video>): Promise<null> { return unwrap(http.put("/video/" + id, data)) }
export function deleteVideo(id: string): Promise<null> { return unwrap(http.delete("/video/" + id)) }
export function onlineVideo(id: string): Promise<Video> { return unwrap(http.put("/video/" + id + "/online")) }
export function offlineVideo(id: string): Promise<Video> { return unwrap(http.put("/video/" + id + "/offline")) }
