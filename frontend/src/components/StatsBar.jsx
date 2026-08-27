import StatusBar from './StatusBar'

const CARDS = [
  ['identities', 'Identities', false],
  ['groups', 'Groups', false],
  ['resources', 'Resources', false],
  ['criticalResources', 'Critical resources', false],
  ['toxicPaths', 'Toxic paths', true],
]

export default function StatsBar({ stats, statusSlot }) {
  return (
    <div className="flex items-center gap-4 border-b border-slate-800 bg-slate-900/40 px-5 py-2.5">
      <div className="flex flex-1 flex-wrap items-center gap-x-6 gap-y-2">
        {CARDS.map(([key, label, danger]) => {
          const value = stats?.[key] ?? '–'
          const alarm = danger && value > 0
          return (
            <div key={key} className="flex items-baseline gap-2">
              <span
                className={`text-lg font-semibold tabular-nums ${alarm ? 'text-rose-400' : 'text-slate-100'}`}
              >
                {value}
              </span>
              <span className={`text-[11px] ${alarm ? 'text-rose-300/80' : 'text-slate-500'}`}>{label}</span>
            </div>
          )
        })}
        {stats?.toxicPaths > 0 && (
          <span className="rounded bg-rose-500/10 px-2 py-0.5 text-[10px] font-medium text-rose-300 ring-1 ring-rose-500/30">
            suspended identities can still reach critical data
          </span>
        )}
      </div>
      <StatusBar health={statusSlot} />
    </div>
  )
}
