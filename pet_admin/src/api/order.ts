import http from './index'
import { unwrap } from './helper'
import type { PageResult } from '../types/api'
import type { Order, OrderStatus } from '../types/order'

export function listOrders(params: {
  page: number
  size: number
  status?: OrderStatus
}): Promise<PageResult<Order>> {
  return unwrap(http.get('/order/list', { params }))
}

export function updateOrderStatus(id: string, status: OrderStatus, reason?: string): Promise<null> {
  return unwrap(http.put('/order/' + id + '/status', { status, reason }))
}

export function reviewReturn(id: string, approved: boolean, reason?: string): Promise<null> {
  return unwrap(http.put('/order/' + id + '/return-review', { approved, reason }))
}

export function getOrderDetail(id: string): Promise<Order> {
  return unwrap(http.get('/order/' + id))
}

export function getDashboardStats(): Promise<{
  userCount: number
  storeCount: number
  productCount: number
  todayOrders: number
  totalRevenue: number
  orderStatusCount: Record<string, number>
}> {
  return unwrap(http.get('/dashboard/stats'))
}