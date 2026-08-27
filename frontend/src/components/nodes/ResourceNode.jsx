import { DatabaseIcon, NodeHandles, SensitivityChip } from '../ui'

const CRITICAL_RING = 'ring-rose-500/70 shadow-[0_0_18px_-4px] shadow-rose-500/50'

export default function ResourceNode({ data }) {
  const { node, selected, dim, cascade } = data
  const critical = node.props.sensitivity === 'Critical'

  return (
    <div
      className={`w-64 rounded-xl bg-slate-900 px-3 py-2.5 shadow-lg shadow-black/30 ring-1 transition-opacity ${
        critical ? CRITICAL_RING : 'ring-slate-700/80'
      } ${cascade ? 'animate-pulse ring-2 ring-rose-400' : ''} ${
        selected ? 'ring-2 ring-indigo-400' : ''
      } ${dim ? 'opacity-15' : ''}`}
    >
      <NodeHandles />
      <div className="flex items-center gap-2.5">
        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-orange-500/15 text-orange-300">
          <DatabaseIcon className="h-4.5 w-4.5" />
        </div>
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-medium text-slate-100">{node.props.name}</p>
          <div className="mt-0.5">
            <SensitivityChip level={node.props.sensitivity} />
          </div>
        </div>
      </div>
    </div>
  )
}
