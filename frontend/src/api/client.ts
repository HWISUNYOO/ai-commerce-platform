import type { Product, Order, CreateOrderItem, AssistantAnswer } from './types'

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (!res.ok) {
    const text = await res.text()
    // 서버 에러 응답이 {code, message} 형태면 message를 사용자에게 그대로 보여준다.
    let message = text
    try {
      const parsed = JSON.parse(text)
      if (parsed?.message) message = parsed.message
    } catch {
      // JSON이 아니면 원문 사용
    }
    throw new Error(message)
  }
  return (await res.json()) as T
}

export const api = {
  getProducts: () => request<Product[]>('/api/products'),
  getProduct: (id: number | string) => request<Product>(`/api/products/${id}`),
  createOrder: (memberId: number, items: CreateOrderItem[]) =>
    request<Order>('/api/orders', {
      method: 'POST',
      body: JSON.stringify({ memberId, items }),
    }),
  askAssistant: (query: string) =>
    request<AssistantAnswer>('/api/ai/assistant', {
      method: 'POST',
      body: JSON.stringify({ query }),
    }),
}
