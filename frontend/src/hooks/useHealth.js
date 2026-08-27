import { useEffect, useState } from 'react'
import { getHealth } from '../api/graph'

const POLL_MS = 20000

/** Polls /api/health so the status pill stays honest even if CognoDB falls asleep mid-session. */
export function useHealth() {
  const [health, setHealth] = useState(null)

  useEffect(() => {
    let alive = true
    const poll = async () => {
      try {
        const h = await getHealth()
        if (alive) setHealth(h)
      } catch {
        if (alive) setHealth({ status: 'down', database: false, latencyMs: -1 })
      }
    }
    poll()
    const timer = setInterval(poll, POLL_MS)
    return () => {
      alive = false
      clearInterval(timer)
    }
  }, [])

  return health
}
