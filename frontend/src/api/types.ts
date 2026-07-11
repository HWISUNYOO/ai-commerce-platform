export interface Product {
  id: number
  name: string
  description: string | null
  price: number
  stockQuantity: number
  status: string
  createdAt: string
}

export interface OrderItem {
  id: number
  productId: number
  productName: string
  unitPrice: number
  quantity: number
  lineTotal: number
}

export interface Order {
  id: number
  memberId: number
  status: string
  totalAmount: number
  items: OrderItem[]
  createdAt: string
}

export interface CreateOrderItem {
  productId: number
  productName: string
  unitPrice: number
  quantity: number
}
