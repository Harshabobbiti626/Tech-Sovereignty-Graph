import { Chip, PanelShell, SensitivityChip, ShieldIcon, UserIcon } from './ui'

const REL_STYLES = {
  MEMBER_OF: 'bg-slate-500/15 text-slate-300 ring-slate-500/40',
  INHERITS: 'bg-violet-500/15 text-violet-300 ring-violet-500/40',
  ACCESS: 'bg-sky-500/15 text-sky-300 ring-sky-500/40',
  DEPENDS_ON: 'bg-orange-500/15 text-orange-300 ring-orange-500/40',
}

const TITLE_BY_TYPE = {
  identity: (p) => p.email,
  group: (p) => p.name,
  resource: (p) => p.name,
}

function DependencyRow({ dep, onSelect }) {
  return (
    <button
      onClick={() => onSelect(dep.id)}
      className="flex w-full items-center gap-2 rounded-lg px-2 py-1.5 text-left transition hover:bg-slate-800/70"
    >
      <span className="min-w-0 flex-1 truncate text-xs text-slate-200">{dep.name}</span>
      <Chip className={REL_STYLES[dep.rel] ?? REL_STYLES.MEMBER_OF}>{dep.rel}</Chip>
    </button>
  )
}

export default function DetailsDrawer({ context, onClose, onSelectNode, onAudit, onImpact }) {
  const { props, type } = context
  const isIdentity = type === 'identity'
  const isGroup = type === 'group'

  return (
    <PanelShell
      title={TITLE_BY_TYPE[type](props)}
      subtitle={type.charAt(0).toUpperCase() + type.slice(1)}
      onClose={onClose}
    >
      <div className="space-y-5">
        <div className="flex items-center gap-2.5 rounded-lg bg-slate-800/50 p-3">
          <div className="flex h-8 w-8 shrink-0 items-center justify-center rounded-md bg-indigo-500/15 text-indigo-300">
            {isIdentity ? <UserIcon className="h-4 w-4" /> : <ShieldIcon className="h-4 w-4" />}
          </div>
          <div className="flex flex-wrap gap-1.5">
            {isIdentity && (
              <>
                <Chip className="bg-slate-700/40 text-slate-300 ring-slate-600/40">{props.role}</Chip>
                <Chip
                  className={
                    props.status === 'Suspended'
                      ? 'bg-rose-500/15 text-rose-300 ring-rose-500/40'
                      : 'bg-emerald-500/15 text-emerald-300 ring-emerald-500/40'
                  }
                >
                  {props.status}
                </Chip>
              </>
            )}
            {!isIdentity && !isGroup && <SensitivityChip level={props.sensitivity} />}
          </div>
        </div>

        <section>
          <h3 className="mb-2 text-[11px] font-semibold tracking-wide text-slate-500 uppercase">
            Properties
          </h3>
          <dl className="divide-y divide-slate-800/80 overflow-hidden rounded-lg ring-1 ring-slate-800">
            {Object.entries(props).map(([key, value]) => (
              <div key={key} className="flex gap-3 px-3 py-1.5 text-xs odd:bg-slate-800/30">
                <dt className="w-24 shrink-0 text-slate-500">{key}</dt>
                <dd className="min-w-0 break-all text-slate-200">{String(value)}</dd>
              </div>
            ))}
          </dl>
        </section>

        <section>
          <h3 className="mb-1.5 text-[11px] font-semibold tracking-wide text-slate-500 uppercase">
            Upstream · {context.upstream.length}
          </h3>
          {context.upstream.length === 0 ? (
            <p className="text-xs text-slate-500">Nothing flows into this node.</p>
          ) : (
            <div className="space-y-0.5">
              {context.upstream.map((dep) => (
                <DependencyRow key={`${dep.rel}:${dep.id}`} dep={dep} onSelect={onSelectNode} />
              ))}
            </div>
          )}
        </section>

        <section>
          <h3 className="mb-1.5 text-[11px] font-semibold tracking-wide text-slate-500 uppercase">
            Downstream · {context.downstream.length}
          </h3>
          {context.downstream.length === 0 ? (
            <p className="text-xs text-slate-500">This node reaches nothing else.</p>
          ) : (
            <div className="space-y-0.5">
              {context.downstream.map((dep) => (
                <DependencyRow key={`${dep.rel}:${dep.id}`} dep={dep} onSelect={onSelectNode} />
              ))}
            </div>
          )}
        </section>

        {isIdentity && (
          <button
            onClick={() => onAudit(props.email, null)}
            className="w-full rounded-lg bg-indigo-500 py-2 text-sm font-medium text-white transition hover:bg-indigo-400"
          >
            Trace all access paths
          </button>
        )}
        {isGroup && (
          <button
            onClick={() => onImpact(props.name)}
            className="w-full rounded-lg bg-violet-500 py-2 text-sm font-medium text-white transition hover:bg-violet-400"
          >
            Run blast radius analysis
          </button>
        )}
      </div>
    </PanelShell>
  )
}
