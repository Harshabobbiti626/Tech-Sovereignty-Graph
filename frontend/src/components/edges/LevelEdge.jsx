import { BaseEdge, EdgeLabelRenderer, getBezierPath, useStore } from '@xyflow/react'

const SHOW_BELOW_ZOOM = 0.3

/** Color language shared by the edge chips and the legend. */
export function levelChipClass(level) {
  switch (level) {
    case 'ADMIN':
      return 'bg-rose-500/20 text-rose-200 ring-rose-400/50'
    case 'WRITE':
      return 'bg-amber-500/20 text-amber-200 ring-amber-400/50'
    case 'READ':
      return 'bg-sky-500/20 text-sky-200 ring-sky-400/50'
    default:
      return 'bg-slate-700/60 text-slate-300 ring-slate-500/50'
  }
}

/**
 * ACCESS edges only: the path plus an HTML level chip that keeps a constant,
 * readable size at any zoom (hidden when zoomed far out to avoid clutter).
 */
export default function LevelEdge({
  id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  style,
  markerEnd,
  data,
}) {
  const [path, labelX, labelY] = getBezierPath({
    sourceX,
    sourceY,
    sourcePosition,
    targetX,
    targetY,
    targetPosition,
  })
  const zoom = useStore((s) => s.transform[2])
  const showChip = data?.level && zoom >= SHOW_BELOW_ZOOM

  return (
    <>
      <BaseEdge id={id} path={path} style={style} markerEnd={markerEnd} />
      {showChip && (
        <EdgeLabelRenderer>
          <div
            className={`nodrag nopan pointer-events-none absolute rounded-full px-1.5 py-px text-[10px] font-semibold ring-1 backdrop-blur-sm ${levelChipClass(
              data.level,
            )} ${data?.dim ? 'opacity-20' : ''}`}
            style={{ transform: `translate(-50%, -50%) translate(${labelX}px, ${labelY}px)` }}
          >
            {data.level}
          </div>
        </EdgeLabelRenderer>
      )}
    </>
  )
}
