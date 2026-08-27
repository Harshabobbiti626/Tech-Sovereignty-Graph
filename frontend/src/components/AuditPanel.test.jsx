import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import AuditPanel from './AuditPanel'

vi.mock('./states/EmptyState', () => ({
  default: () => <div data-testid="empty" />,
}))

const identity = { email: 'intern_developer@wexa.ai', role: 'Intern', status: 'Active' }

describe('AuditPanel', () => {
  it('shows the toxic banner when a suspended identity reaches critical data', () => {
    render(
      <AuditPanel
        result={{
          identity: { ...identity, email: 'former_vendor_consultant@external.com', status: 'Suspended' },
          paths: [{ length: 4, toxic: true, resourceName: 'Customer_PII_Database', sensitivity: 'Critical', steps: [] }],
          toxicCount: 1,
        }}
        onClose={vi.fn()}
      />,
    )
    expect(screen.getByText(/toxic access detected/i)).toBeInTheDocument()
    expect(screen.getByText('Customer_PII_Database')).toBeInTheDocument()
  })

  it('shows the all-clear state when there is nothing to report', () => {
    render(
      <AuditPanel result={{ identity, paths: [], toxicCount: 0 }} onClose={vi.fn()} />,
    )
    expect(screen.getByTestId('empty')).toBeInTheDocument()
  })
})
