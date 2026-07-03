import http from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { User } from "../types/user"
export function listUsers(params: { page: number; size: number; keyword?: string }): Promise<PageResult<User>> { return unwrap(http.get("/user/search", { params })) }
export function updateUser(id: string, data: Partial<User>): Promise<void> { return unwrap(http.put("/user/" + id, data)) }
export function deleteUser(id: string): Promise<void> { return unwrap(http.delete("/user/" + id)) }
