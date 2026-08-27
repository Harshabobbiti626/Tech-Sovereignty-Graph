export default function StatusBar({ health }) {
  if (!health) {
    return (
      <span className="flex items-center gap-1.5 rounded-full bg-slate-800/70 px-2.5 py-1 text-[11px] text-slate-400 ring-1 ring-slate-700">
        <span className="h-1.5 w-1.5 animate-pulse rounded-full bg-slate-500" />
        checking CognoDB…
      </span>
    )
  }

  const up = health.database
  return (
    <span
      className={`flex items-center gap-1.5 rounded-full px-2.5 py-1 text-[11px] ring-1 ${
        up
          ? 'bg-emerald-500/10 text-emerald-300 ring-emerald-500/30'
          : 'bg-rose-500/10 text-rose-300 ring-rose-500/30'
      }`}
      title={`GET /api/health → ${health.status}`}
    >
      <span className={`h-1.5 w-1.5 rounded-full ${up ? 'bg-emerald-400' : 'bg-rose-400 animate-pulse'}`} />
      {up ? `CognoDB · ${health.latencyMs}ms` : 'CognoDB unreachable'}
    </span>
  )
}
