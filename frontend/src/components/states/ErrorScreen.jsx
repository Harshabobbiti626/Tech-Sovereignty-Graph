import { useEffect, useState } from 'react'

/**
 * Branded outage screen. The API's 503 body carries the breaker's retry window,
 * so the countdown mirrors what the backend is actually doing.
 */
export default function ErrorScreen({ error, onRetry }) {
  const totalSeconds = Math.ceil((error?.retryInMs ?? 15000) / 1000)
  const [seconds, setSeconds] = useState(totalSeconds)

  useEffect(() => {
    if (seconds <= 0) {
      onRetry()
      return
    }
    const timer = setTimeout(() => setSeconds((s) => s - 1), 1000)
    return () => clearTimeout(timer)
  }, [seconds, onRetry])

  const apiDown = error?.status === 0

  return (
    <div className="flex h-full flex-col items-center justify-center gap-6 bg-slate-950 px-6 text-center">
      <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-amber-500/10 ring-1 ring-amber-500/30">
        <svg viewBox="0 0 24 24" className="h-8 w-8 text-amber-400" fill="none" stroke="currentColor" strokeWidth="1.5">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
          <path d="M12 8v4M12 16h.01" strokeLinecap="round" />
        </svg>
      </div>

      <div className="max-w-md space-y-2">
        <h1 className="text-lg font-semibold text-slate-100">Wexa Context Engine</h1>
        <p className="text-sm text-slate-400">
          {apiDown
            ? 'The gateway cannot reach the API server.'
            : 'The gateway is re-establishing a secure link to CognoDB.'}
        </p>
        <p className="text-xs text-slate-500">
          {apiDown ? 'Check that the backend is running.' : error?.message}
        </p>
      </div>

      <div className="flex items-center gap-3">
        <span className="flex h-9 min-w-9 items-center justify-center rounded-full bg-slate-800 px-3 text-sm font-semibold tabular-nums text-slate-200 ring-1 ring-slate-700">
          {Math.max(seconds, 0)}s
        </span>
        <span className="text-xs text-slate-500">auto-retry countdown</span>
        <button
          onClick={onRetry}
          className="rounded-lg bg-indigo-500 px-4 py-2 text-sm font-medium text-white transition hover:bg-indigo-400"
        >
          Retry now
        </button>
      </div>
    </div>
  )
}
