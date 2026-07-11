import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import type { Product } from '../api/types'

export interface CartItem {
  product: Product
  qty: number
}

interface CartValue {
  items: CartItem[]
  add: (product: Product, qty: number) => void
  setQty: (productId: number, qty: number) => void
  remove: (productId: number) => void
  clear: () => void
  count: number
  total: number
}

const CartContext = createContext<CartValue | null>(null)
const CART_KEY = 'cart.items'

export function CartProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<CartItem[]>(() => {
    const raw = localStorage.getItem(CART_KEY)
    return raw ? (JSON.parse(raw) as CartItem[]) : []
  })

  useEffect(() => {
    localStorage.setItem(CART_KEY, JSON.stringify(items))
  }, [items])

  function add(product: Product, qty: number) {
    setItems((prev) => {
      const existing = prev.find((i) => i.product.id === product.id)
      const nextQty = (existing?.qty ?? 0) + qty
      const capped = Math.min(nextQty, product.stockQuantity)
      if (existing) {
        return prev.map((i) => (i.product.id === product.id ? { ...i, qty: capped } : i))
      }
      return [...prev, { product, qty: capped }]
    })
  }

  function setQty(productId: number, qty: number) {
    setItems((prev) =>
      prev.map((i) =>
        i.product.id === productId
          ? { ...i, qty: Math.min(Math.max(1, qty), i.product.stockQuantity) }
          : i,
      ),
    )
  }

  function remove(productId: number) {
    setItems((prev) => prev.filter((i) => i.product.id !== productId))
  }

  function clear() {
    setItems([])
  }

  const count = items.reduce((sum, i) => sum + i.qty, 0)
  const total = items.reduce((sum, i) => sum + i.product.price * i.qty, 0)

  return (
    <CartContext.Provider value={{ items, add, setQty, remove, clear, count, total }}>
      {children}
    </CartContext.Provider>
  )
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used within CartProvider')
  return ctx
}
