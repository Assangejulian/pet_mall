export type OrderStatus =
  | '0'
  | '1'
  | '2'
  | '3'
  | '4'
  | '-1'
  | '-2'
  | '-3'
  | '-4'

export interface Order {
  id: string
  orderNo: string
  userId: string
  userName: string
  totalAmount: number
  status: OrderStatus
  address: string
  createTime: string
  payTime?: string
  cancelReason?: string
  cancelType?: 'user' | 'timeout'
  returnReason?: string
  returnApplyTime?: string
  shipTime?: string
  receiveTime?: string
  evaluateTime?: string
  items: OrderItem[]
}

export interface OrderItem {
  id: string
  productId: string
  productName: string
  productImage?: string
  quantity: number
  price: number
  evaluateStar?: number
  evaluateContent?: string
}