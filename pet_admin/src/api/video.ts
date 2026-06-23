import http from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Video } from "../types/video"
export function listVideos(params: { page: number; size: number }): Promise<PageResult<Video>> { return unwrap(http.get("/video/list", { params })) }
export function deleteVideo(id: string): Promise<null> { return unwrap(http.delete("/video/" + id)) }
