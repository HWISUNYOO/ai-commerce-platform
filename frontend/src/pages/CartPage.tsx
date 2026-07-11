import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { api } from '../api/client'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'

const won = (n: number) => n.toLocaleString('ko-KR') + '원'

export default function CartPage() {
  const { items, setQty, remove, clear, total } = useCart()
  const { member } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  async function checkout() {
    if (!member) return
    setBusy(true)
    setError(null)
    try {
      const order = await api.createOrder(
        member.id,
        items.map((i) => ({
          productId: i.product.id,
          productName: i.product.name,
          unitPrice: i.product.price,
          quantity: i.qty,
        })),
      )
      clear()
      navigate(`/orders/${order.id}`, { state: order })
    } catch (e) {
      setError(String(e instanceof Error ? e.message : e))
      setBusy(false)
    }
  }

  if (items.length === 0) {
    return (
      <div className="text-center text-gray-500">
        <p>장바구니가 비어 있습니다.</p>
        <Link to="/" className="mt-4 inline-block text-indigo-600">
          ← 쇼핑하러 가기
        </Link>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-2xl">
      <h1 className="mb-6 text-2xl font-bold">장바구니</h1>

      <div className="divide-y rounded-lg border bg-white">
        {items.map((i) => (
          <div key={i.product.id} className="flex items-center gap-4 p-4">
            <div className="flex h-14 w-14 items-center justify-center rounded bg-gray-100 text-2xl">
              {i.product.imageEmoji ?? '📦'}
            </div>
            <div className="flex-1">
              <p className="font-medium">{i.product.name}</p>
              <p className="text-sm text-gray-500">{won(i.product.price)}</p>
            </div>
            <input
              type="number"
              min={1}
              max={i.product.stockQuantity}
              value={i.qty}
              onChange={(e) => setQty(i.product.id, Number(e.target.value))}
              className="w-16 rounded border px-2 py-1"
            />
            <span className="w-24 text-right font-medium">{won(i.product.price * i.qty)}</span>
            <button
              onClick={() => remove(i.product.id)}
              className="text-sm text-gray-400 hover:text-red-600"
            >
              ✕
            </button>
          </div>
        ))}
      </div>

      <div className="mt-4 flex items-center justify-between text-lg font-bold">
        <span>합계</span>
        <span className="text-indigo-600">{won(total)}</span>
      </div>

      {error && <p className="mt-3 text-sm text-red-600">{error}</p>}

      {member ? (
        <button
          onClick={checkout}
          disabled={busy}
          className="mt-6 w-full rounded-lg bg-indigo-600 px-4 py-3 font-semibold text-white hover:bg-indigo-700 disabled:opacity-50"
        >
          {busy ? '주문 중…' : `${member.name}님으로 주문하기`}
        </button>
      ) : (
        <div className="mt-6 rounded-lg bg-amber-50 p-4 text-center text-sm text-amber-800">
          주문하려면{' '}
          <Link to="/login" className="font-semibold text-indigo-600">
            로그인
          </Link>
          이 필요합니다.
        </div>
      )}
    </div>
  )
}
