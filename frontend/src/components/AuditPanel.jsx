import { Chip, PanelShell, SensitivityChip } from './ui'
import EmptyState from './states/EmptyState'

function PathCard({ path, active, onClick }) {
  return (
    <button
      onClick={onClick}
      className={`w-full rounded-lg p-2.5 text-left ring-1 transition ${
        active ? 'bg-slate-800 ring-indigo-400/60' : 'bg-slate-800/40 ring-slate-800 hover:ring-slate-600'
      }`}
    >
      <div className="flex items-center gap-2">
        <span className="min-w-0 flex-1 truncate text-xs font-medium text-slate-100">
          {path.resourceName}
        </span>
        <SensitivityChip level={path.sensitivity} />
        <span className="text-[10px] text-slate-500">{path.length} hops</span>
      </div>
      <div className="mt-2 flex flex-wrap items-center gap-x-1 gap-y-1">
        {path.steps.map((step, i) => (
          <span key={i} className="flex items-center gap-1">
            {i > 0 && (
              <span className="text-[9px] text-slate-500">
                —{step.relType}
                {step.level ? `·${step.level}` : ''}→
              </span>
            )}
            <span
              className={`rounded px-1 py-0.5 text-[10px] ${
                i === path.steps.length - 1
                  ? 'bg-slate-700/70 text-slate-200'
                  : 'bg-slate-800 text-slate-400'
              }`}
            >
              {step.nodeName}
            </span>
          </span>
        ))}
      </div>
    </button>
  )
}

export default function AuditPanel({ result, error, selectedPath, onSelectPath, onClose }) {
  if (error) {
    return (
      <PanelShell title="Access path audit" onClose={onClose}>
        <p className="rounded-lg bg-rose-500/10 p-3 text-xs text-rose-300 ring-1 ring-rose-500/30">
          {error.message}
        </p>
      </PanelShell>
    )
  }

  const { identity, paths, toxicCount } = result

  return (
    <PanelShell
      title="Access path audit"
      subtitle={`${identity.email} · ${identity.role}`}
      onClose={onClose}
    >
      <div className="space-y-4">
        <Chip
          className={
            identity.status === 'Suspended'
              ? 'bg-rose-500/15 text-rose-300 ring-rose-500/40'
              : 'bg-emerald-500/15 text-emerald-300 ring-emerald-500/40'
          }
        >
          {identity.status}
        </Chip>

        {toxicCount > 0 && (
          <div className="rounded-lg bg-rose-500/10 p-3 ring-1 ring-rose-500/30">
            <p className="text-xs font-semibold text-rose-300">
              ⚠ Toxic access detected — {toxicCount} path{toxicCount > 1 ? 's' : ''} from a suspended
              identity to critical resources
            </p>
            <p className="mt-1 text-[11px] text-rose-300/70">
              Recursive group inheritance keeps this account alive; revocation has to happen at the
              group level.
            </p>
          </div>
        )}

        {paths.length === 0 ? (
          <EmptyState
            title="No risky permissions found"
            body="This identity has no traversable access path to the selected resource."
          />
        ) : (
          <div className="space-y-2">
            <h3 className="text-[11px] font-semibold tracking-wide text-slate-500 uppercase">
              {paths.length} path{paths.length > 1 ? 's' : ''} found
            </h3>
            {paths.map((path, i) => (
              <PathCard key={i} path={path} active={selectedPath === i} onClick={() => onSelectPath(i)} />
            ))}
          </div>
        )}
      </div>
    </PanelShell>
  )
}
