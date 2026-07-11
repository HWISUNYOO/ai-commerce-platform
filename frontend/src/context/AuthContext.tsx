import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import { api } from '../api/client'
import type { Member } from '../api/types'

interface AuthValue {
  member: Member | null
  token: string | null
  login: (email: string, password: string) => Promise<void>
  register: (email: string, password: string, name: string) => Promise<void>
  logout: () => void
}

const AuthContext = createContext<AuthValue | null>(null)
const TOKEN_KEY = 'auth.token'
const MEMBER_KEY = 'auth.member'

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY))
  const [member, setMember] = useState<Member | null>(() => {
    const raw = localStorage.getItem(MEMBER_KEY)
    return raw ? (JSON.parse(raw) as Member) : null
  })

  // 저장된 토큰이 있으면 /me로 유효성 검증 후 세션 복원(만료/위조면 로그아웃).
  useEffect(() => {
    if (!token) return
    api
      .me(token)
      .then((m) => {
        setMember(m)
        localStorage.setItem(MEMBER_KEY, JSON.stringify(m))
      })
      .catch(() => logout())
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function persist(t: string, m: Member) {
    setToken(t)
    setMember(m)
    localStorage.setItem(TOKEN_KEY, t)
    localStorage.setItem(MEMBER_KEY, JSON.stringify(m))
  }

  async function login(email: string, password: string) {
    const res = await api.login(email, password)
    persist(res.token, res.member)
  }

  async function register(email: string, password: string, name: string) {
    await api.register(email, password, name)
    await login(email, password) // 가입 후 자동 로그인
  }

  function logout() {
    setToken(null)
    setMember(null)
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(MEMBER_KEY)
  }

  return (
    <AuthContext.Provider value={{ member, token, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
