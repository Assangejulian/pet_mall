import http from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Order, OrderStatus } from "../types/order"
export function listOrders(params: { page: number; size: number; status?: OrderStatus }): Promise<PageResult<Order>> { return unwrap(http.get("/order/list", { params })) }
export function updateOrderStatus(id: string, status: OrderStatus): Promise<null> { return unwrap(http.put("/order/" + id + "/status", { status })) }
