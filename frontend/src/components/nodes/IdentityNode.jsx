import { NodeHandles, UserIcon } from '../ui'

const STATUS = {
  Active: { dot: 'bg-emerald-400', ring: 'ring-slate-700/80' },
  Suspended: { dot: 'bg-rose-500', ring: 'ring-rose-500/70' },
  Automated: { dot: 'bg-sky-400', ring: 'ring-slate-700/80' },
}

export default function IdentityNode({ data }) {
  const { node, selected, dim } = data
  const status = STATUS[node.props.status] ?? STATUS.Active
  const bot = node.props.status === 'Automated'

  return (
    <div
      className={`w-64 rounded-xl bg-slate-900 px-3 py-2.5 shadow-lg shadow-black/30 ring-1 transition-opacity ${
        status.ring
      } ${selected ? 'ring-2 ring-indigo-400' : ''} ${dim ? 'opacity-15' : ''}`}
    >
      <NodeHandles />
      <div className="flex items-center gap-2.5">
        <div
          className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full ${
            bot ? 'bg-sky-500/15 text-sky-300' : 'bg-indigo-500/15 text-indigo-300'
          }`}
        >
          <UserIcon className="h-4.5 w-4.5" />
        </div>
        <div className="min-w-0 flex-1">
          <p className="truncate text-sm font-medium text-slate-100">{node.props.email}</p>
          <div className="mt-0.5 flex items-center gap-1.5">
            <span className="truncate text-[10px] text-slate-400">{node.props.role}</span>
            <span className="ml-auto flex shrink-0 items-center gap-1 text-[10px] text-slate-400">
              <span className={`h-1.5 w-1.5 rounded-full ${status.dot}`} />
              {node.props.status}
            </span>
          </div>
        </div>
      </div>
    </div>
  )
}
