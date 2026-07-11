import { useState } from 'react'
import { api } from '../api/client'
import type { AssistantAnswer } from '../api/types'

// AI 쇼핑 어시스턴트: 자연어 질문 → 에이전트가 도구(상품검색)를 호출해 추천을 답한다.
export default function AssistantBox() {
  const [query, setQuery] = useState('')
  const [result, setResult] = useState<AssistantAnswer | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [showTrace, setShowTrace] = useState(false)

  const ask = async () => {
    if (!query.trim() || loading) return
    setLoading(true)
    setError(null)
    setResult(null)
    try {
      setResult(await api.askAssistant(query))
    } catch (e) {
      setError(String(e))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="mb-8 rounded-lg border border-indigo-200 bg-indigo-50 p-4">
      <h2 className="mb-2 font-semibold text-indigo-900">🤖 AI 쇼핑 어시스턴트</h2>
      <div className="flex gap-2">
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && ask()}
          placeholder="예: 5만원 이하로 게임할 때 쓸 거 추천해줘"
          className="flex-1 rounded border border-gray-300 bg-white px-3 py-2 outline-none focus:border-indigo-400"
        />
        <button
          onClick={ask}
          disabled={loading}
          className="rounded bg-indigo-600 px-4 py-2 font-medium text-white transition hover:bg-indigo-700 disabled:opacity-50"
        >
          {loading ? '생각 중…' : '추천받기'}
        </button>
      </div>

      {error && <p className="mt-2 text-sm text-red-600">에러: {error}</p>}

      {result && (
        <div className="mt-3 rounded bg-white p-3 shadow-sm">
          <p className="whitespace-pre-wrap text-gray-800">{result.answer}</p>

          {result.steps.length > 0 && (
            <div className="mt-3 border-t pt-2">
              <button
                onClick={() => setShowTrace((v) => !v)}
                className="text-xs text-indigo-600 hover:underline"
              >
                {showTrace
                  ? '▲ 도구 호출 숨기기'
                  : `▼ AI가 사용한 도구 ${result.steps.length}건 보기`}
              </button>
              {showTrace && (
                <ul className="mt-2 space-y-1">
                  {result.steps.map((s, i) => (
                    <li key={i} className="font-mono text-xs text-gray-500">
                      🔧 {s.action}
                      <span className="text-gray-400">({s.args})</span>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}

          <p className="mt-2 text-right text-xs text-gray-400">
            응답 생성: {result.backend}
          </p>
        </div>
      )}
    </div>
  )
}
