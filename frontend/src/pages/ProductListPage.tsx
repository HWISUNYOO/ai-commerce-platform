import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { api } from '../api/client'
import type { Product } from '../api/types'
import AssistantBox from '../components/AssistantBox'

const won = (n: number) => n.toLocaleString('ko-KR') + '원'

export default function ProductListPage() {
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api
      .getProducts()
      .then(setProducts)
      .catch((e) => setError(String(e)))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div>
      <AssistantBox />
      <h1 className="mb-6 text-2xl font-bold">상품</h1>
      {loading ? (
        <p className="text-gray-500">불러오는 중…</p>
      ) : error ? (
        <p className="text-red-600">에러: {error}</p>
      ) : products.length === 0 ? (
        <p className="text-gray-500">등록된 상품이 없습니다.</p>
      ) : (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {products.map((p) => (
            <Link
              key={p.id}
              to={`/products/${p.id}`}
              className="rounded-lg border bg-white p-4 shadow-sm transition hover:shadow-md"
            >
              <div className="flex h-32 items-center justify-center rounded bg-gray-100 text-5xl">
                {p.imageEmoji ?? '📦'}
              </div>
              <h2 className="mt-3 font-semibold">{p.name}</h2>
              <p className="mt-1 text-lg font-bold text-indigo-600">{won(p.price)}</p>
              <p className="mt-1 text-sm text-gray-500">
                {p.stockQuantity > 0 ? `재고 ${p.stockQuantity}개` : '품절'}
              </p>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
