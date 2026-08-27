import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import StatsBar from './StatsBar'

const base = { identities: 19, groups: 9, resources: 12, criticalResources: 2, toxicPaths: 0 }

describe('StatsBar', () => {
  it('renders the counts', () => {
    render(<StatsBar stats={base} statusSlot={null} />)
    expect(screen.getByText('19')).toBeInTheDocument()
    expect(screen.getByText('Critical resources')).toBeInTheDocument()
  })

  it('raises the alarm and a note when toxic paths exist', () => {
    render(<StatsBar stats={{ ...base, toxicPaths: 4 }} statusSlot={null} />)
    expect(screen.getByText('4')).toHaveClass('text-rose-400')
    expect(screen.getByText(/suspended identities can still reach critical data/i)).toBeInTheDocument()
  })
})
