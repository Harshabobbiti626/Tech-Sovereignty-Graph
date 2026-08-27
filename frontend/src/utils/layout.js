import dagre from '@dagrejs/dagre'

const SIZES = {
  identity: { width: 256, height: 76 },
  group: { width: 224, height: 60 },
  resource: { width: 256, height: 76 },
}

const sizeOf = (node) => SIZES[node.type] ?? SIZES.group

/** Left-to-right layered layout: identities feed groups, groups feed resources. */
export function layoutGraph(nodes, edges) {
  const g = new dagre.graphlib.Graph()
  g.setGraph({ rankdir: 'LR', nodesep: 34, ranksep: 140, marginx: 24, marginy: 24 })
  g.setDefaultEdgeLabel(() => ({}))

  nodes.forEach((n) => g.setNode(n.id, sizeOf(n)))
  edges.forEach((e) => g.setEdge(e.source, e.target))
  dagre.layout(g)

  return nodes.map((node) => {
    const pos = g.node(node.id)
    const size = sizeOf(node)
    return { ...node, position: { x: pos.x - size.width / 2, y: pos.y - size.height / 2 } }
  })
}
