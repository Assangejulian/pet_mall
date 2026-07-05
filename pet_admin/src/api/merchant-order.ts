import { merchantHttp } from "./index"
import { unwrap } from "./helper"
import type { PageResult } from "../types/api"
import type { Order, OrderStatus } from "../types/order"

export function listMerchantOrders(params: {
  current: number
  size: number
  orderStatus?: string
}): Promise<PageResult<Order>> {
  return unwrap(merchantHttp.get("/order/search", { params }))
}

export function shipMerchantOrder(orderId: string): Promise<void> {
  return unwrap(merchantHttp.put("/order/ship", { orderId }))
}

