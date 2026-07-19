import { useEffect, useState } from 'react'
import { Link, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import { useCart } from './context/CartContext'
import { api } from './api/client'

export default function App() {
  const { member, logout } = useAuth()
  const { count } = useCart()
  const location = useLocation()
  const [balance, setBalance] = useState<number | null>(null)

  // 로그인 회원의 포인트 잔액을 헤더에 표시. 페이지 이동 시마다 갱신해
  // 주문 후 적립된 포인트가 반영되도록 한다.
  useEffect(() => {
    if (!member) {
      setBalance(null)
      return
    }
    api
      .getPointBalance(member.id)
      .then((b) => setBalance(b.balance))
      .catch(() => setBalance(null))
  }, [member, location.pathname])

  return (
    <div className="min-h-screen bg-gray-50 text-gray-900">
      <header className="border-b bg-white">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
          <Link to="/" className="text-xl font-bold">
            🛒 AI Commerce
          </Link>
          <nav className="flex items-center gap-4 text-sm">
            <Link to="/cart" className="hover:text-indigo-600">
              장바구니
              {count > 0 && (
                <span className="ml-1 rounded-full bg-indigo-600 px-2 py-0.5 text-xs text-white">
                  {count}
                </span>
              )}
            </Link>
            {member ? (
              <>
                <Link to="/mypage" className="hover:text-indigo-600">
                  🔔 알림
                </Link>
                <Link
                  to="/mypage"
                  className="rounded-full bg-amber-100 px-2 py-0.5 text-xs font-semibold text-amber-700 hover:bg-amber-200"
                  title="보유 포인트"
                >
                  {balance !== null ? `${balance.toLocaleString('ko-KR')}P` : '…P'}
                </Link>
                <Link to="/mypage" className="text-gray-600 hover:text-indigo-600">
                  {member.name}님
                </Link>
                <button onClick={logout} className="text-gray-400 hover:text-red-600">
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="hover:text-indigo-600">
                  로그인
                </Link>
                <Link
                  to="/register"
                  className="rounded bg-indigo-600 px-3 py-1 text-white hover:bg-indigo-700"
                >
                  회원가입
                </Link>
              </>
            )}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-5xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
