export interface Product {
  id: number
  name: string
  description: string | null
  price: number
  stockQuantity: number
  imageEmoji: string | null
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

export interface Member {
  id: number
  email: string
  name: string
  status: string
  createdAt: string
}

export interface LoginResponse {
  token: string
  member: Member
}

// AI 쇼핑 어시스턴트(에이전트) 응답
export interface AssistantStep {
  action: string
  args: string
  observation: string
}

export interface AssistantAnswer {
  answer: string
  steps: AssistantStep[]
  backend: string
}
