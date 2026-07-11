import { Link, useLocation } from 'react-router-dom'
import type { Order } from '../api/types'

const won = (n: number) => n.toLocaleString('ko-KR') + '원'

export default function OrderCompletePage() {
  const { state } = useLocation()
  const order = state as Order | null

  if (!order) {
    return (
      <div className="text-center">
        <p className="text-gray-500">주문 정보를 찾을 수 없습니다.</p>
        <Link to="/" className="mt-4 inline-block text-indigo-600">
          ← 홈으로
        </Link>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-lg">
      <div className="rounded-lg border bg-white p-6 text-center shadow-sm">
        <div className="text-5xl">✅</div>
        <h1 className="mt-3 text-2xl font-bold">주문 완료!</h1>
        <p className="mt-2 text-gray-600">
          주문번호 <b>#{order.id}</b> · 상태 <b>{order.status}</b>
        </p>
        <p className="mt-1 text-sm text-gray-500">
          결제는 Kafka 이벤트로 결제 서비스가 자동 처리합니다.
        </p>

        <div className="mt-6 divide-y text-left">
          {order.items.map((it) => (
            <div key={it.id} className="flex justify-between py-2">
              <span>
                {it.productName} × {it.quantity}
              </span>
              <span>{won(it.lineTotal)}</span>
            </div>
          ))}
        </div>
        <div className="mt-4 flex justify-between border-t pt-4 text-lg font-bold">
          <span>합계</span>
          <span className="text-indigo-600">{won(order.totalAmount)}</span>
        </div>

        <Link
          to="/"
          className="mt-6 inline-block rounded-lg bg-gray-100 px-4 py-2 text-sm hover:bg-gray-200"
        >
          계속 쇼핑하기
        </Link>
      </div>
    </div>
  )
}
