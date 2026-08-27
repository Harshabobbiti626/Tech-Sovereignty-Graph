export default function LoadingScreen() {
  return (
    <div className="flex h-full flex-col items-center justify-center gap-6 bg-slate-950">
      <div className="relative flex h-20 w-20 items-center justify-center">
        <span className="absolute h-full w-full animate-ping rounded-full bg-indigo-500/20" />
        <span className="absolute h-14 w-14 animate-ping rounded-full bg-indigo-500/25 [animation-delay:150ms]" />
        <svg viewBox="0 0 24 24" className="relative h-9 w-9 text-indigo-400" fill="none" stroke="currentColor" strokeWidth="1.5">
          <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
        </svg>
      </div>
      <div className="space-y-2 text-center">
        <p className="text-sm font-medium text-slate-200">Traversing the governance graph…</p>
        <p className="text-xs text-slate-500">fetching identities, groups and resources from CognoDB</p>
      </div>
    </div>
  )
}
