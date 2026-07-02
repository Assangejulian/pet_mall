import http from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Order, OrderStatus } from "../types/order"

export function listOrders(params: {
  page: number
  size: number
  orderStatus?: string
}): Promise<PageResult<Order>> {
  return unwrap(http.get("/order/search", { params }))
}

export function updateOrderStatus(id: string, status: OrderStatus, reason?: string): Promise<null> {
  return unwrap(http.put("/order/" + id, { status: status, cancelReason: reason }))
}

export function reviewReturn(id: string, approved: boolean, reason?: string): Promise<null> {
  return unwrap(http.put("/order/" + id, { status: approved ? "-3" : "3", returnAuditOpinion: reason }))
}

export function getOrderDetail(id: string): Promise<Order> {
  return unwrap(http.get("/order/" + id))
}
