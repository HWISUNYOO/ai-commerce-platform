import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { api } from '../api/client'
import type { Product } from '../api/types'

const won = (n: number) => n.toLocaleString('ko-KR') + '원'
// 데모용 회원 ID. 로그인 기능은 이후 단계에서 추가한다.
const DEMO_MEMBER_ID = 1

export default function ProductDetailPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [product, setProduct] = useState<Product | null>(null)
  const [qty, setQty] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [ordering, setOrdering] = useState(false)

  useEffect(() => {
    if (!id) return
    api
      .getProduct(id)
      .then(setProduct)
      .catch((e) => setError(String(e)))
      .finally(() => setLoading(false))
  }, [id])

  async function placeOrder() {
    if (!product) return
    setOrdering(true)
    setError(null)
    try {
      const quantity = Math.min(product.stockQuantity, Math.max(1, qty))
      const order = await api.createOrder(DEMO_MEMBER_ID, [
        {
          productId: product.id,
          productName: product.name,
          unitPrice: product.price,
          quantity,
        },
      ])
      navigate(`/orders/${order.id}`, { state: order })
    } catch (e) {
      setError(String(e))
      setOrdering(false)
    }
  }

  if (loading) return <p className="text-gray-500">불러오는 중…</p>
  if (error && !product) return <p className="text-red-600">에러: {error}</p>
  if (!product) return null

  const soldOut = product.stockQuantity <= 0
  const cappedQty = Math.min(qty, Math.max(1, product.stockQuantity))

  return (
    <div>
      <Link to="/" className="text-sm text-indigo-600">
        ← 목록으로
      </Link>
      <div className="mt-4 grid gap-8 sm:grid-cols-2">
        <div className="flex h-64 items-center justify-center rounded-lg bg-gray-100 text-8xl">
          {product.imageEmoji ?? '📦'}
        </div>
        <div>
          <h1 className="text-2xl font-bold">{product.name}</h1>
          <p className="mt-2 text-gray-600">{product.description}</p>
          <p className="mt-4 text-3xl font-bold text-indigo-600">{won(product.price)}</p>
          <p className="mt-1 text-sm text-gray-500">
            {soldOut ? '품절' : `재고 ${product.stockQuantity}개`}
          </p>

          <div className="mt-6 flex items-center gap-3">
            <label className="text-sm">수량</label>
            <input
              type="number"
              min={1}
              max={product.stockQuantity}
              value={cappedQty}
              disabled={soldOut}
              onChange={(e) =>
                setQty(
                  Math.min(
                    product.stockQuantity,
                    Math.max(1, Number(e.target.value)),
                  ),
                )
              }
              className="w-20 rounded border px-2 py-1 disabled:bg-gray-100"
            />
            <span className="text-sm text-gray-500">합계 {won(product.price * cappedQty)}</span>
          </div>

          <button
            onClick={placeOrder}
            disabled={ordering || soldOut}
            className="mt-6 w-full rounded-lg bg-indigo-600 px-4 py-3 font-semibold text-white transition hover:bg-indigo-700 disabled:opacity-50"
          >
            {soldOut ? '품절' : ordering ? '주문 중…' : '주문하기'}
          </button>
          {error && <p className="mt-2 text-sm text-red-600">{error}</p>}
        </div>
      </div>
    </div>
  )
}
