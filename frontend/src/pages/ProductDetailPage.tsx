import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { api } from '../api/client'
import type { Product } from '../api/types'
import { useCart } from '../context/CartContext'

const won = (n: number) => n.toLocaleString('ko-KR') + '원'

export default function ProductDetailPage() {
  const { id } = useParams()
  const { add } = useCart()
  const [product, setProduct] = useState<Product | null>(null)
  const [qty, setQty] = useState(1)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [added, setAdded] = useState(false)

  useEffect(() => {
    if (!id) return
    api
      .getProduct(id)
      .then(setProduct)
      .catch((e) => setError(String(e)))
      .finally(() => setLoading(false))
  }, [id])

  if (loading) return <p className="text-gray-500">불러오는 중…</p>
  if (error && !product) return <p className="text-red-600">에러: {error}</p>
  if (!product) return null

  const soldOut = product.stockQuantity <= 0
  const cappedQty = Math.min(qty, Math.max(1, product.stockQuantity))

  function addToCart() {
    if (!product) return
    add(product, cappedQty)
    setAdded(true)
  }

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
              onChange={(e) => {
                setQty(Math.min(product.stockQuantity, Math.max(1, Number(e.target.value))))
                setAdded(false)
              }}
              className="w-20 rounded border px-2 py-1 disabled:bg-gray-100"
            />
            <span className="text-sm text-gray-500">합계 {won(product.price * cappedQty)}</span>
          </div>

          <button
            onClick={addToCart}
            disabled={soldOut}
            className="mt-6 w-full rounded-lg bg-indigo-600 px-4 py-3 font-semibold text-white transition hover:bg-indigo-700 disabled:opacity-50"
          >
            {soldOut ? '품절' : '장바구니 담기'}
          </button>

          {added && (
            <p className="mt-3 text-center text-sm text-green-700">
              장바구니에 담았습니다.{' '}
              <Link to="/cart" className="font-semibold text-indigo-600">
                장바구니 보기 →
              </Link>
            </p>
          )}
        </div>
      </div>
    </div>
  )
}
