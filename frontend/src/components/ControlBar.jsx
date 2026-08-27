import { useState } from 'react'
import { ShieldIcon } from './ui'

export default function ControlBar({
  identities,
  resourceNames,
  onAudit,
  onReset,
  onSimulate,
  canSimulate,
  busy,
}) {
  const [email, setEmail] = useState('')
  const [resource, setResource] = useState('')

  const submit = (e) => {
    e.preventDefault()
    if (email.trim()) onAudit(email.trim(), resource || null)
  }

  return (
    <header className="flex flex-wrap items-center gap-x-6 gap-y-3 border-b border-slate-800 bg-slate-900/40 px-5 py-3">
      <div className="flex items-center gap-2.5">
        <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-indigo-500/15 text-indigo-300 ring-1 ring-indigo-500/30">
          <ShieldIcon className="h-5 w-5" />
        </div>
        <div>
          <h1 className="text-sm font-semibold leading-tight text-slate-100">Tech Sovereignty</h1>
          <p className="text-[10px] leading-tight text-slate-500">Shadow IT &amp; governance graph</p>
        </div>
      </div>

      <form onSubmit={submit} className="flex flex-1 items-center gap-2">
        <input
          list="identity-emails"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="audit an identity — e.g. former_vendor_consultant@external.com"
          className="h-9 min-w-0 flex-1 rounded-lg border-0 bg-slate-800/70 px-3 text-sm text-slate-200 ring-1 ring-slate-700 outline-none placeholder:text-slate-500 focus:ring-2 focus:ring-indigo-500"
        />
        <datalist id="identity-emails">
          {identities.map((email) => (
            <option key={email} value={email} />
          ))}
        </datalist>

        <select
          value={resource}
          onChange={(e) => setResource(e.target.value)}
          className="h-9 rounded-lg border-0 bg-slate-800/70 px-2 text-sm text-slate-300 ring-1 ring-slate-700 outline-none focus:ring-2 focus:ring-indigo-500"
        >
          <option value="">any resource</option>
          {resourceNames.map((name) => (
            <option key={name} value={name}>
              {name}
            </option>
          ))}
        </select>

        <button
          type="submit"
          disabled={!email.trim() || busy}
          className="h-9 rounded-lg bg-indigo-500 px-4 text-sm font-medium text-white transition hover:bg-indigo-400 disabled:cursor-not-allowed disabled:opacity-40"
        >
          Audit
        </button>
      </form>

      <div className="flex items-center gap-2">
        {canSimulate && (
          <button
            onClick={onSimulate}
            disabled={busy}
            className="h-9 rounded-lg bg-rose-600 px-3.5 text-sm font-medium text-white transition hover:bg-rose-500 disabled:cursor-not-allowed disabled:opacity-40"
          >
            Simulate compromise
          </button>
        )}
        <button
          onClick={onReset}
          className="h-9 rounded-lg px-3 text-sm text-slate-400 transition hover:bg-slate-800 hover:text-slate-200"
        >
          Reset view
        </button>
      </div>
    </header>
  )
}
