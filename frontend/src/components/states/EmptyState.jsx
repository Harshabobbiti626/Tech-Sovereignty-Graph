export default function EmptyState({ title, body }) {
  return (
    <div className="flex flex-col items-center justify-center rounded-lg bg-slate-800/30 px-6 py-10 text-center ring-1 ring-slate-800">
      <svg viewBox="0 0 24 24" className="h-8 w-8 text-emerald-400" fill="none" stroke="currentColor" strokeWidth="1.5">
        <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        <path d="m9 12 2 2 4-4" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
      <p className="mt-3 text-sm font-medium text-slate-200">{title}</p>
      <p className="mt-1 text-xs text-slate-500">{body}</p>
    </div>
  )
}
