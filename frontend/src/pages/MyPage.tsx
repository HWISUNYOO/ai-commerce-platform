import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { api } from '../api/client'
import type { Notification, PointTransaction } from '../api/types'

const dt = (s: string) => new Date(s).toLocaleString('ko-KR')

// 알림 종류별 아이콘/라벨
const NOTIF_META: Record<string, { icon: string; label: string }> = {
  ORDER_CREATED: { icon: '📦', label: '주문' },
  PAYMENT_APPROVED: { icon: '💳', label: '결제' },
  POINT_EARNED: { icon: '⭐', label: '적립' },
}

export default function MyPage() {
  const { member } = useAuth()
  const [balance, setBalance] = useState(0)
  const [history, setHistory] = useState<PointTransaction[]>([])
  const [notifs, setNotifs] = useState<Notification[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!member) return
    setLoading(true)
    Promise.all([
      api.getPointBalance(member.id),
      api.getPointHistory(member.id),
      api.getNotifications(member.id),
    ])
      .then(([b, h, n]) => {
        setBalance(b.balance)
        setHistory(h)
        setNotifs(n)
      })
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [member])

  if (!member) {
    return (
      <div className="text-center">
        <p className="text-gray-500">로그인이 필요합니다.</p>
        <Link to="/login" className="mt-4 inline-block text-indigo-600">
          로그인하러 가기 →
        </Link>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      <h1 className="text-2xl font-bold">마이페이지</h1>

      {/* 포인트 잔액 */}
      <section className="rounded-lg border bg-gradient-to-r from-indigo-500 to-violet-500 p-6 text-white shadow-sm">
        <p className="text-sm opacity-90">{member.name}님의 보유 포인트</p>
        <p className="mt-1 text-4xl font-bold">
          {balance.toLocaleString('ko-KR')}
          <span className="ml-1 text-2xl">P</span>
        </p>
      </section>

      {/* 포인트 적립 내역 */}
      <section>
        <h2 className="mb-3 text-lg font-semibold">포인트 내역</h2>
        <div className="overflow-hidden rounded-lg border bg-white shadow-sm">
          {loading ? (
            <p className="p-6 text-center text-gray-400">불러오는 중…</p>
          ) : history.length === 0 ? (
            <p className="p-6 text-center text-gray-400">아직 적립 내역이 없습니다.</p>
          ) : (
            <table className="w-full text-sm">
              <thead className="bg-gray-50 text-left text-gray-500">
                <tr>
                  <th className="px-4 py-2">일시</th>
                  <th className="px-4 py-2">주문번호</th>
                  <th className="px-4 py-2">구분</th>
                  <th className="px-4 py-2 text-right">포인트</th>
                </tr>
              </thead>
              <tbody className="divide-y">
                {history.map((h) => (
                  <tr key={h.id}>
                    <td className="px-4 py-2 text-gray-500">{dt(h.createdAt)}</td>
                    <td className="px-4 py-2">#{h.orderId}</td>
                    <td className="px-4 py-2">{h.type === 'EARN' ? '적립' : '사용'}</td>
                    <td className="px-4 py-2 text-right font-semibold text-indigo-600">
                      +{h.amount.toLocaleString('ko-KR')}P
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </section>

      {/* 알림 */}
      <section>
        <h2 className="mb-3 text-lg font-semibold">🔔 알림</h2>
        <div className="space-y-2">
          {loading ? (
            <p className="p-6 text-center text-gray-400">불러오는 중…</p>
          ) : notifs.length === 0 ? (
            <p className="rounded-lg border bg-white p-6 text-center text-gray-400 shadow-sm">
              알림이 없습니다.
            </p>
          ) : (
            notifs.map((n) => {
              const meta = NOTIF_META[n.type] ?? { icon: '🔔', label: '알림' }
              return (
                <div
                  key={n.id}
                  className="flex items-start gap-3 rounded-lg border bg-white p-4 shadow-sm"
                >
                  <span className="text-xl">{meta.icon}</span>
                  <div className="flex-1">
                    <p className="text-sm">{n.message}</p>
                    <p className="mt-1 text-xs text-gray-400">{dt(n.createdAt)}</p>
                  </div>
                </div>
              )
            })
          )}
        </div>
      </section>
    </div>
  )
}
