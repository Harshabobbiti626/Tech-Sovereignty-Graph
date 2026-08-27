import { describe, expect, it } from 'vitest'
import { layoutGraph } from './layout'

const node = (id, type, props) => ({ id, type, props })

const graph = [
  node('res_slack', 'resource', { name: 'Slack_Public_Channels', sensitivity: 'Low' }),
  node('id_cto', 'identity', { email: 'sarah.kim@wexa.ai' }),
  node('gr_eng', 'group', { name: 'Engineering' }),
  node('res_pii', 'resource', { name: 'Customer_PII_Database', sensitivity: 'Critical' }),
]

describe('layoutGraph', () => {
  it('places each type in its own column', () => {
    const positioned = layoutGraph(graph)
    const byId = Object.fromEntries(positioned.map((n) => [n.id, n.position.x]))
    expect(byId.id_cto).toBe(0)
    expect(byId.gr_eng).toBeGreaterThan(0)
    expect(byId.res_pii).toBeGreaterThan(byId.gr_eng)
  })

  it('stacks resources critical-first, then alphabetical', () => {
    const [pii, slack] = layoutGraph(graph).filter((n) => n.type === 'resource')
    expect(pii.id).toBe('res_pii')
    expect(slack.id).toBe('res_slack')
    expect(pii.position.y).toBeLessThan(slack.position.y)
  })

  it('keeps nodes with unknown types instead of dropping them', () => {
    const positioned = layoutGraph([node('weird', 'mystery', { name: '?' })])
    expect(positioned).toHaveLength(1)
  })
})
