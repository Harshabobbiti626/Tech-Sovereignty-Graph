const SIZE = {
  identity: { width: 256, height: 76, row: 94 },
  group: { width: 224, height: 60, row: 94 },
  resource: { width: 256, height: 76, row: 94 },
}

const COLUMN_X = { identity: 0, group: 400, resource: 800 }

const SEVERITY = { Critical: 0, High: 1, Medium: 2, Low: 3 }

// fixed tiers (identities -> groups -> resources); stable even with disconnected nodes
export function layoutGraph(nodes) {
  const tiers = { identity: [], group: [], resource: [] }
  nodes.forEach((node) => {
    // unknown labels land in the group tier rather than vanishing
    const tier = tiers[(node.type ?? 'group').toLowerCase()] ?? tiers.group
    tier.push(node)
  })

  const midY = (Math.max(tiers.identity.length, tiers.group.length, tiers.resource.length, 1) * SIZE.group.row) / 2
  const positioned = []

  for (const [type, list] of Object.entries(tiers)) {
    const { width, row } = SIZE[type]
    const tierHeight = (list.length * row) / 2
    const startY = midY - tierHeight
    const sorted = [...list].sort((a, b) => rank(a) - rank(b) || nameOf(a).localeCompare(nameOf(b)))
    sorted.forEach((node, i) => {
      positioned.push({
        ...node,
        position: { x: COLUMN_X[type] + (width === 224 ? 16 : 0), y: startY + i * row },
      })
    })
  }
  return positioned
}

const nameOf = (node) => node.props.name ?? node.props.email ?? node.id
const rank = (node) =>
  node.type === 'resource' ? (SEVERITY[node.props.sensitivity] ?? 4) : 0
