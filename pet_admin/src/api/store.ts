import http from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Store } from "../types/store"
export function listStores(params: { page: number; size: number; keyword?: string; status?: number }): Promise<PageResult<Store>> { return unwrap(http.get("/store/list", { params })) }
export function updateStore(id: string, data: Partial<Store>): Promise<null> { return unwrap(http.put("/store/" + id, data)) }
