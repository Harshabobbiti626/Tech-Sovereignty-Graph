import { Handle, Position } from '@xyflow/react'

export const SENSITIVITY_STYLES = {
  Critical: 'bg-rose-500/15 text-rose-300 ring-rose-500/40',
  High: 'bg-amber-500/15 text-amber-300 ring-amber-500/40',
  Medium: 'bg-sky-500/15 text-sky-300 ring-sky-500/40',
  Low: 'bg-emerald-500/15 text-emerald-300 ring-emerald-500/40',
}

export function Chip({ children, className = '' }) {
  return (
    <span
      className={`inline-flex items-center rounded px-1.5 py-0.5 text-[10px] font-medium ring-1 ring-inset ${className}`}
    >
      {children}
    </span>
  )
}

export function SensitivityChip({ level }) {
  return (
    <Chip className={SENSITIVITY_STYLES[level] ?? 'bg-slate-500/15 text-slate-300 ring-slate-500/40'}>
      {level}
    </Chip>
  )
}

export function NodeHandles() {
  const style = { background: '#475569', border: 'none', width: 7, height: 7 }
  return (
    <>
      <Handle type="target" position={Position.Left} style={style} />
      <Handle type="source" position={Position.Right} style={style} />
    </>
  )
}

/* feather-style stroke icons keep the bundle lean (no icon library) */
const iconProps = {
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 2,
  strokeLinecap: 'round',
  strokeLinejoin: 'round',
}

export function UserIcon({ className }) {
  return (
    <svg {...iconProps} className={className}>
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>
  )
}

export function ShieldIcon({ className }) {
  return (
    <svg {...iconProps} className={className}>
      <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
    </svg>
  )
}

export function DatabaseIcon({ className }) {
  return (
    <svg {...iconProps} className={className}>
      <ellipse cx="12" cy="5" rx="9" ry="3" />
      <path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3" />
      <path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5" />
    </svg>
  )
}

/** Shared drawer/panel chrome so the right side always feels like one surface. */
export function PanelShell({ title, subtitle, onClose, children }) {
  return (
    <aside className="flex w-[400px] shrink-0 flex-col border-l border-slate-800 bg-slate-900/60 backdrop-blur">
      <header className="flex items-start justify-between gap-3 border-b border-slate-800 px-4 py-3">
        <div className="min-w-0">
          <h2 className="truncate text-sm font-semibold text-slate-100">{title}</h2>
          {subtitle && <p className="mt-0.5 text-xs text-slate-400">{subtitle}</p>}
        </div>
        <button
          onClick={onClose}
          className="rounded p-1 text-slate-400 transition hover:bg-slate-800 hover:text-slate-200"
          aria-label="Close panel"
        >
          <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M18 6 6 18M6 6l12 12" />
          </svg>
        </button>
      </header>
      <div className="slim-scroll min-h-0 flex-1 overflow-y-auto px-4 py-3">{children}</div>
    </aside>
  )
}
