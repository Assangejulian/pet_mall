import { publicHttp } from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Order, OrderStatus } from "../types/order"

export function listOrders(params: {
  page: number
  size: number
  orderStatus?: string
}): Promise<PageResult<Order>> {
  return unwrap(publicHttp.get("/order/search", { params }))
}

export function updateOrderStatus(id: string, status: OrderStatus, reason?: string): Promise<null> {
  return unwrap(publicHttp.put("/order/" + id, { orderStatus: status, cancelReason: reason }))
}

export function reviewReturn(id: string, approved: boolean, reason?: string): Promise<null> {
  return unwrap(publicHttp.put("/order/" + id, { orderStatus: approved ? "-3" : "3", returnAuditOpinion: reason }))
}

export function getOrderDetail(id: string): Promise<Order> {
  return unwrap(publicHttp.get("/order/" + id))
}
