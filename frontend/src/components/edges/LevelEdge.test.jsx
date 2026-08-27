import { describe, expect, it } from 'vitest'
import { levelChipClass } from './LevelEdge'

describe('levelChipClass', () => {
  it('color-codes each access level', () => {
    expect(levelChipClass('ADMIN')).toContain('rose')
    expect(levelChipClass('WRITE')).toContain('amber')
    expect(levelChipClass('READ')).toContain('sky')
  })

  it('falls back to neutral for unknown levels', () => {
    expect(levelChipClass('OWNER')).toContain('slate')
    expect(levelChipClass(undefined)).toContain('slate')
  })
})
