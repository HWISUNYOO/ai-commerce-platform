import type { Product, Order, CreateOrderItem, AssistantAnswer } from './types'

async function request<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(url, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  if (!res.ok) {
    throw new Error(`${res.status} ${await res.text()}`)
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
