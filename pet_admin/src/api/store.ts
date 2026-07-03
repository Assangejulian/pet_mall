import http from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Store } from "../types/store"
export function listStores(params: { current: number; size: number; keyword?: string; status?: number; city?: string }): Promise<PageResult<Store>> { return unwrap(http.get("/store/search", { params })) }
export function updateStore(id: string, data: Partial<Store>): Promise<void> { return unwrap(http.put("/store/" + id, data)) }
