import { useMemo } from 'react'
import { Background, BackgroundVariant, Controls, MarkerType, MiniMap, ReactFlow } from '@xyflow/react'
import '@xyflow/react/dist/style.css'
import IdentityNode from './nodes/IdentityNode'
import GroupNode from './nodes/GroupNode'
import ResourceNode from './nodes/ResourceNode'

const nodeTypes = { identity: IdentityNode, group: GroupNode, resource: ResourceNode }

const EDGE_BASE = {
  MEMBER_OF: { stroke: '#64748b' },
  INHERITS: { stroke: '#a855f7', strokeDasharray: '7 4' },
  ACCESS: { stroke: '#0ea5e9' },
  DEPENDS_ON: { stroke: '#f97316', strokeDasharray: '2 4' },
}

const LEGEND = [
  ['MEMBER_OF', '#64748b', false],
  ['INHERITS', '#a855f7', true],
  ['ACCESS', '#0ea5e9', false],
  ['DEPENDS_ON', '#f97316', true],
]

export default function GraphCanvas({ data, highlight, cascadeIds, selectedId, onNodeClick }) {
  const { nodes, edges } = useMemo(() => {
    const lit = highlight && (highlight.nodes.size > 0 || highlight.edges.size > 0)

    const rfNodes = data.nodes.map((node) => ({
      id: node.id,
      type: node.type,
      position: node.position,
      data: {
        node,
        selected: node.id === selectedId,
        cascade: cascadeIds?.has(node.id) ?? false,
        dim: lit && !highlight.nodes.has(node.id),
      },
    }))

    const rfEdges = data.edges.map((edge) => {
      const hot = Boolean(lit && highlight.edges.has(edge.id))
      const dim = Boolean(lit && !hot)
      const base = EDGE_BASE[edge.type] ?? EDGE_BASE.MEMBER_OF
      return {
        id: edge.id,
        source: edge.source,
        target: edge.target,
        label: edge.type === 'ACCESS' ? edge.props?.level : undefined,
        animated: hot,
        style: hot
          ? { ...base, strokeWidth: 3 }
          : { ...base, strokeWidth: 1.2, opacity: dim ? 0.07 : 0.8 },
        labelStyle: { fill: '#94a3b8', fontSize: 9 },
        labelBgStyle: { fill: '#0f172a' },
        labelBgPadding: [4, 2],
        labelBgBorderRadius: 4,
        markerEnd: { type: MarkerType.ArrowClosed, color: hot ? base.stroke : '#334155' },
      }
    })

    return { nodes: rfNodes, edges: rfEdges }
  }, [data, highlight, cascadeIds, selectedId])

  return (
    <div className="relative h-full w-full">
      <ReactFlow
        nodes={nodes}
        edges={edges}
        nodeTypes={nodeTypes}
        onNodeClick={(_, node) => onNodeClick(node.id)}
        colorMode="dark"
        fitView
        fitViewOptions={{ padding: 0.15 }}
        minZoom={0.15}
        deleteKeyCode={null}
        onInit={(instance) => {
          // fitView fires before nodes are measured, so re-fit once layout settles
          setTimeout(() => instance.fitView({ padding: 0.15, duration: 300 }), 350)
        }}
      >
        <Background variant={BackgroundVariant.Dots} gap={22} size={1.2} color="#1e293b" />
        <Controls showInteractive={false} className="!shadow-none" />
        <MiniMap pannable zoomable bgColor="#0f172a" maskColor="rgba(15, 23, 42, 0.7)" />
      </ReactFlow>

      <div className="pointer-events-none absolute bottom-3 left-3 flex items-center gap-3 rounded-lg bg-slate-900/80 px-3 py-1.5 ring-1 ring-slate-800">
        {LEGEND.map(([label, color, dashed]) => (
          <span key={label} className="flex items-center gap-1.5 text-[10px] text-slate-400">
            <span
              className="inline-block h-0 w-4 border-t-2"
              style={{ borderColor: color, borderStyle: dashed ? 'dashed' : 'solid' }}
            />
            {label}
          </span>
        ))}
      </div>
    </div>
  )
}
