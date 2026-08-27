import { useCallback, useEffect, useState } from 'react'
import { getGraph, getStats } from '../api/graph'

/** Loads the full graph plus header stats; 'error' state powers the branded outage screen. */
export function useGraphData() {
  const [phase, setPhase] = useState('loading')
  const [data, setData] = useState(null)
  const [stats, setStats] = useState(null)
  const [error, setError] = useState(null)

  const refresh = useCallback(async () => {
    setPhase('loading')
    try {
      const [graph, stats] = await Promise.all([getGraph(), getStats()])
      setData(graph)
      setStats(stats)
      setPhase('ready')
    } catch (err) {
      setError(err)
      setPhase('error')
    }
  }, [])

  useEffect(() => {
    refresh()
  }, [refresh])

  return { phase, data, stats, error, refresh }
}
