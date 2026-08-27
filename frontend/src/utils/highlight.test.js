import { describe, expect, it } from 'vitest'
import { compromisedResourceIds, pathHighlights } from './highlight'

const piiPath = {
  resourceId: 'res_pii',
  steps: [
    { nodeId: 'id_vendor', relType: null },
    { nodeId: 'gr_legacy', relType: 'MEMBER_OF' },
    { nodeId: 'res_pii', relType: 'ACCESS' },
  ],
}
const keysPath = {
  resourceId: 'res_keys',
  steps: [
    { nodeId: 'id_vendor', relType: null },
    { nodeId: 'gr_legacy', relType: 'INHERITS' },
    { nodeId: 'res_keys', relType: 'ACCESS' },
  ],
}

describe('pathHighlights', () => {
  it('collects every node on the paths', () => {
    const { nodes } = pathHighlights([piiPath, keysPath])
    expect([...nodes].sort()).toEqual(['gr_legacy', 'id_vendor', 'res_keys', 'res_pii'])
  })

  it('builds edge ids that match the canvas convention source:type:target', () => {
    const { edges } = pathHighlights([piiPath])
    expect([...edges]).toEqual(['id_vendor:MEMBER_OF:gr_legacy', 'gr_legacy:ACCESS:res_pii'])
  })

  it('returns empty sets for empty input', () => {
    const { nodes, edges } = pathHighlights([])
    expect(nodes.size).toBe(0)
    expect(edges.size).toBe(0)
  })
})

describe('compromisedResourceIds', () => {
  it('picks the resource at the end of each path', () => {
    const ids = compromisedResourceIds([piiPath, keysPath])
    expect([...ids].sort()).toEqual(['res_keys', 'res_pii'])
  })
})
