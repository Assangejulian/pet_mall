import http from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { User } from "../types/user"
export function listUsers(params: { page: number; size: number; keyword?: string }): Promise<PageResult<User>> { return unwrap(http.get("/user/list", { params })) }
export function updateUser(id: string, data: Partial<User>): Promise<null> { return unwrap(http.put("/user/" + id, data)) }
export function deleteUser(id: string): Promise<null> { return unwrap(http.delete("/user/" + id)) }
