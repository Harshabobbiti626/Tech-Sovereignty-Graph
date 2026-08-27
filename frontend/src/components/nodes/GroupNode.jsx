import { NodeHandles, ShieldIcon } from '../ui'

export default function GroupNode({ data }) {
  const { node, selected, dim } = data

  return (
    <div
      className={`w-56 rounded-lg bg-slate-900 px-3 py-2 shadow-md shadow-black/30 ring-1 transition-opacity ${
        selected ? 'ring-2 ring-indigo-400' : 'ring-slate-700/80'
      } ${dim ? 'opacity-15' : ''}`}
    >
      <NodeHandles />
      <div className="flex items-center gap-2">
        <div className="flex h-7 w-7 shrink-0 items-center justify-center rounded-md bg-violet-500/15 text-violet-300">
          <ShieldIcon className="h-4 w-4" />
        </div>
        <p className="truncate text-sm font-medium text-slate-100">{node.props.name}</p>
      </div>
    </div>
  )
}
