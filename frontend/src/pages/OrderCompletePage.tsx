import { useEffect, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { api } from '../api/client'
import type { Notification, Order } from '../api/types'

const won = (n: number) => n.toLocaleString('ko-KR') + '원'
const NOTIF_ICON: Record<string, string> = {
  ORDER_CREATED: '📦',
  PAYMENT_APPROVED: '💳',
  POINT_EARNED: '⭐',
  ORDER_CANCELLED: '⚠️',
}

// 주문 상태별 헤더 표현
const STATUS_VIEW: Record<string, { icon: string; title: string; color: string }> = {
  PENDING: { icon: '⏳', title: '주문 처리 중…', color: 'text-amber-600' },
  CONFIRMED: { icon: '✅', title: '주문 완료!', color: 'text-emerald-600' },
  CANCELLED: { icon: '❌', title: '주문이 취소되었습니다', color: 'text-red-600' },
}

export default function OrderCompletePage() {
  const { state } = useLocation()
  const initial = state as Order | null

  const [status, setStatus] = useState<string>(initial?.status ?? 'PENDING')
  const [earnedPoints, setEarnedPoints] = useState<number | null>(null)
  const [notifs, setNotifs] = useState<Notification[]>([])

  // 주문 상태(재고예약→결제→확정/취소)와 적립·알림은 Kafka Saga 로 비동기 처리되므로,
  // 주문 직후 잠깐 폴링해 최종 결과를 화면에 반영한다(최대 10회, 1초 간격).
  useEffect(() => {
    if (!initial) return
    let tries = 0
    const timer = setInterval(async () => {
      tries++
      try {
        const [order, history, all] = await Promise.all([
          api.getOrder(initial.id),
          api.getPointHistory(initial.memberId),
          api.getNotifications(initial.memberId),
        ])
        setStatus(order.status)
        const earned = history.find((h) => h.orderId === initial.id)
        if (earned) setEarnedPoints(earned.amount)
        setNotifs(all.filter((n) => n.referenceId === initial.id))
        // 종결 상태(확정/취소)에 도달하면 폴링 종료
        if (order.status === 'CONFIRMED' || order.status === 'CANCELLED') {
          clearInterval(timer)
        }
      } catch {
        // 무시하고 다음 폴링
      }
      if (tries >= 10) clearInterval(timer)
    }, 1000)
    return () => clearInterval(timer)
  }, [initial])

  if (!initial) {
    return (
      <div className="text-center">
        <p className="text-gray-500">주문 정보를 찾을 수 없습니다.</p>
        <Link to="/" className="mt-4 inline-block text-indigo-600">
          ← 홈으로
        </Link>
      </div>
    )
  }

  const view = STATUS_VIEW[status] ?? STATUS_VIEW.PENDING
  const cancelled = status === 'CANCELLED'

  return (
    <div className="mx-auto max-w-lg">
      <div className="rounded-lg border bg-white p-6 text-center shadow-sm">
        <div className="text-5xl">{view.icon}</div>
        <h1 className={`mt-3 text-2xl font-bold ${view.color}`}>{view.title}</h1>
        <p className="mt-2 text-gray-600">
          주문번호 <b>#{initial.id}</b> · 상태 <b>{status}</b>
        </p>

        <div className="mt-6 divide-y text-left">
          {initial.items.map((it) => (
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
          <span className="text-indigo-600">{won(initial.totalAmount)}</span>
        </div>

        {/* 포인트 적립 배너 (취소된 주문엔 표시하지 않음) */}
        {!cancelled && (
          <div className="mt-4 rounded-lg bg-amber-50 p-3 text-sm text-amber-800">
            {earnedPoints !== null ? (
              <>⭐ <b>{earnedPoints.toLocaleString('ko-KR')}P</b> 적립 완료!</>
            ) : (
              <span className="text-amber-700/70">⏳ 포인트 적립 처리 중…</span>
            )}
          </div>
        )}

        <Link
          to="/"
          className="mt-6 inline-block rounded-lg bg-gray-100 px-4 py-2 text-sm hover:bg-gray-200"
        >
          계속 쇼핑하기
        </Link>
      </div>

      {/* Saga 진행 현황 (주문→재고예약→결제→적립→알림) */}
      <div className="mt-6 rounded-lg border bg-white p-5 shadow-sm">
        <p className="mb-1 text-sm font-semibold">처리 현황</p>
        <p className="mb-3 text-xs text-gray-400">
          재고예약·결제·적립·알림은 Kafka 이벤트(Saga)로 각 서비스가 자동 처리합니다.
        </p>
        {notifs.length === 0 ? (
          <p className="text-sm text-gray-400">⏳ 이벤트 처리를 기다리는 중…</p>
        ) : (
          <ul className="space-y-2">
            {[...notifs]
              .sort((a, b) => a.id - b.id)
              .map((n) => (
                <li key={n.id} className="flex items-start gap-2 text-sm">
                  <span>{NOTIF_ICON[n.type] ?? '🔔'}</span>
                  <span>{n.message}</span>
                </li>
              ))}
          </ul>
        )}
      </div>
    </div>
  )
}
