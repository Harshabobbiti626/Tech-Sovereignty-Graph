import { PanelShell, SensitivityChip } from './ui'
import EmptyState from './states/EmptyState'

export default function ImpactPanel({ result, onClose }) {
  const { group, affected } = result

  return (
    <PanelShell
      title="Blast radius analysis"
      subtitle={`What goes dark if "${group}" is revoked`}
      onClose={onClose}
    >
      {affected.length === 0 ? (
        <EmptyState
          title="Revoking this group strands nothing"
          body="No resources are reachable through this group's inheritance chain."
        />
      ) : (
        <div className="space-y-2">
          <p className="text-xs text-slate-400">
            {affected.length} resource{affected.length > 1 ? 's' : ''} rely on access paths flowing
            through this group:
          </p>
          {affected.map((row) => (
            <div
              key={row.resourceId}
              className="flex items-center gap-2 rounded-lg bg-slate-800/40 p-2.5 ring-1 ring-slate-800"
            >
              <span className="min-w-0 flex-1 truncate text-xs font-medium text-slate-100">
                {row.resource}
              </span>
              <SensitivityChip level={row.sensitivity} />
              <span className="text-[10px] text-slate-500">
                {row.pathsAtRisk} path{row.pathsAtRisk > 1 ? 's' : ''}
              </span>
            </div>
          ))}
          <p className="pt-1 text-[11px] text-slate-500">
            Depth analysed: up to 3 nested INHERITS hops — the part recursive SQL CTEs choke on.
          </p>
        </div>
      )}
    </PanelShell>
  )
}
