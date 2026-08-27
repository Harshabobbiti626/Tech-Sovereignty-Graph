import { useMemo, useState } from 'react'
import { getAudit, getImpact, getNodeContext } from './api/graph'
import GraphCanvas from './components/GraphCanvas'
import ControlBar from './components/ControlBar'
import StatsBar from './components/StatsBar'
import DetailsDrawer from './components/DetailsDrawer'
import AuditPanel from './components/AuditPanel'
import ImpactPanel from './components/ImpactPanel'
import LoadingScreen from './components/states/LoadingScreen'
import ErrorScreen from './components/states/ErrorScreen'
import { useGraphData } from './hooks/useGraphData'
import { useHealth } from './hooks/useHealth'
import { layoutGraph } from './utils/layout'
import { compromisedResourceIds, pathHighlights } from './utils/highlight'

const CLOSED = { mode: 'closed' }

export default function App() {
  const { phase, data, stats, error, refresh } = useGraphData()
  const health = useHealth()

  const [panel, setPanel] = useState(CLOSED)
  const [highlight, setHighlight] = useState(null)
  const [cascadeIds, setCascadeIds] = useState(null)
  const [selectedId, setSelectedId] = useState(null)
  const [selectedPath, setSelectedPath] = useState(null)
  const [busy, setBusy] = useState(false)

  const graph = useMemo(
    () => (data ? { nodes: layoutGraph(data.nodes, data.edges), edges: data.edges } : null),
    [data],
  )

  const identities = useMemo(
    () =>
      (data?.nodes ?? [])
        .filter((n) => n.type === 'identity')
        .map((n) => n.props.email)
        .sort(),
    [data],
  )

  const resourceNames = useMemo(
    () =>
      (data?.nodes ?? [])
        .filter((n) => n.type === 'resource')
        .map((n) => n.props.name)
        .sort(),
    [data],
  )

  const selectedNode = data?.nodes.find((n) => n.id === selectedId) ?? null

  const resetView = () => {
    setPanel(CLOSED)
    setHighlight(null)
    setCascadeIds(null)
    setSelectedId(null)
    setSelectedPath(null)
  }

  const selectNode = async (id) => {
    setSelectedId(id)
    setSelectedPath(null)
    setCascadeIds(null)
    setPanel({ mode: 'context', loading: true })
    try {
      const context = await getNodeContext(id)
      setPanel({ mode: 'context', context })
      // spotlight the immediate neighborhood while the drawer is open
      const nodes = new Set([id])
      const edges = new Set()
      context.upstream.forEach((d) => {
        nodes.add(d.id)
        edges.add(`${d.id}:${d.rel}:${id}`)
      })
      context.downstream.forEach((d) => {
        nodes.add(d.id)
        edges.add(`${id}:${d.rel}:${d.id}`)
      })
      setHighlight({ nodes, edges })
    } catch (err) {
      if (err.status === 404) resetView()
      else setPanel({ mode: 'context', error: err })
    }
  }

  const runAudit = async (email, resource = null, simulate = false) => {
    setBusy(true)
    setSelectedId(null)
    setSelectedPath(null)
    setPanel({ mode: 'audit', loading: true })
    try {
      const result = await getAudit(email, resource)
      setPanel({ mode: 'audit', result })
      setHighlight(pathHighlights(result.paths))
      setCascadeIds(simulate ? compromisedResourceIds(result.paths) : null)
    } catch (err) {
      setPanel({ mode: 'audit', error: err })
    } finally {
      setBusy(false)
    }
  }

  const runImpact = async (group) => {
    setBusy(true)
    setSelectedId(null)
    setSelectedPath(null)
    setPanel({ mode: 'impact', loading: true })
    try {
      const result = await getImpact(group)
      setPanel({ mode: 'impact', result })
      // light up the group plus every resource caught in its blast radius
      const groupNode = data?.nodes.find((n) => n.type === 'group' && n.props.name === result.group)
      const nodes = new Set(result.affected.map((row) => row.resourceId))
      if (groupNode) nodes.add(groupNode.id)
      result.affected.forEach((row) => nodes.add(row.resourceId))
      setHighlight({ nodes, edges: new Set() })
      setCascadeIds(null)
    } catch (err) {
      setPanel({ mode: 'impact', error: err })
    } finally {
      setBusy(false)
    }
  }

  const selectPath = (index) => {
    setSelectedPath(index)
    const result = panel.result
    setHighlight(pathHighlights([result.paths[index]]))
  }

  if (phase === 'loading') return <LoadingScreen />
  if (phase === 'error') return <ErrorScreen error={error} onRetry={refresh} />

  return (
    <div className="flex h-full flex-col">
      <ControlBar
        identities={identities}
        resourceNames={resourceNames}
        onAudit={runAudit}
        onReset={resetView}
        onSimulate={() => runAudit(selectedNode.props.email, null, true)}
        canSimulate={selectedNode?.type === 'identity'}
        busy={busy}
      />
      <StatsBar stats={stats} statusSlot={health} />

      <div className="flex min-h-0 flex-1">
        <div className="min-w-0 flex-1">
          <GraphCanvas
            data={graph}
            highlight={highlight}
            cascadeIds={cascadeIds}
            selectedId={selectedId}
            onNodeClick={selectNode}
          />
        </div>

        {panel.mode === 'context' &&
          (panel.context ? (
            <DetailsDrawer
              context={panel.context}
              onClose={resetView}
              onSelectNode={selectNode}
              onAudit={runAudit}
              onImpact={runImpact}
            />
          ) : (
            <aside className="w-[400px] shrink-0 border-l border-slate-800 bg-slate-900/60 p-4">
              <p className="animate-pulse text-sm text-slate-400">Loading node context…</p>
            </aside>
          ))}

        {panel.mode === 'audit' &&
          (panel.result ? (
            <AuditPanel
              result={panel.result}
              selectedPath={selectedPath}
              onSelectPath={selectPath}
              onClose={resetView}
            />
          ) : (
            <aside className="w-[400px] shrink-0 border-l border-slate-800 bg-slate-900/60 p-4">
              <p className="animate-pulse text-sm text-slate-400">Walking inheritance chains…</p>
            </aside>
          ))}

        {panel.mode === 'impact' &&
          (panel.result ? (
            <ImpactPanel result={panel.result} onClose={resetView} />
          ) : (
            <aside className="w-[400px] shrink-0 border-l border-slate-800 bg-slate-900/60 p-4">
              <p className="animate-pulse text-sm text-slate-400">Measuring blast radius…</p>
            </aside>
          ))}
      </div>
    </div>
  )
}
