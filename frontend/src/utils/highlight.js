/** Node/edge ids to spotlight, derived from audit path steps (server returns them in walk order). */
export function pathHighlights(paths) {
  const nodes = new Set()
  const edges = new Set()
  for (const path of paths ?? []) {
    const steps = path.steps ?? []
    steps.forEach((step) => nodes.add(step.nodeId))
    for (let i = 1; i < steps.length; i++) {
      edges.add(`${steps[i - 1].nodeId}:${steps[i].relType}:${steps[i].nodeId}`)
    }
  }
  return { nodes, edges }
}

/** Resources an attacker from this identity could actually touch. */
export function compromisedResourceIds(paths) {
  const ids = new Set()
  for (const path of paths ?? []) {
    const last = path.steps?.[path.steps.length - 1]
    if (last) ids.add(last.nodeId)
  }
  return ids
}
