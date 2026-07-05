import http from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Order, OrderStatus } from "../types/order"

export function listOrders(params: {
  current: number
  size: number
  orderStatus?: string
}): Promise<PageResult<Order>> {
  return unwrap(http.get("/order/search", { params }))
}

export function shipOrderAdmin(orderId: string): Promise<void> {
  return unwrap(http.put("/order/ship", { orderId }))
}

export function cancelOrderAdmin(orderId: string, reason?: string): Promise<void> {
  return unwrap(http.put("/order/cancel", { orderId, cancelReason: reason }))
}

export function reviewReturn(orderId: string, approved: boolean, reason?: string): Promise<void> {
  return unwrap(http.put("/order/refund/approve", { orderId, approved, rejectReason: reason }))
}

export function directReturnAdmin(orderId: string, reason?: string): Promise<void> {
  return unwrap(http.put("/order/refund/direct", { orderId, cancelReason: reason }))
}

export function getOrderDetail(id: string): Promise<Order> {
  return unwrap(http.get("/order/" + id))
}
