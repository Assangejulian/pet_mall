export type OrderStatus = 'pending' | 'paid' | 'shipped' | 'completed' | 'cancelled'
export interface Order { id: string; orderNo: string; userId: string; userName: string; totalAmount: number; status: OrderStatus; address: string; createTime: string; items: OrderItem[] }
export interface OrderItem { id: string; productId: string; productName: string; quantity: number; price: number }