import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, api } from './client'

afterEach(() => vi.unstubAllGlobals())

describe('api', () => {
  it('returns the parsed body on success', async () => {
    vi.stubGlobal('fetch', vi.fn(async () => Response.json({ ok: true })))
    await expect(api('/api/health')).resolves.toEqual({ ok: true })
  })

  it('throws an ApiError carrying retryInMs from 503 bodies', async () => {
    vi.stubGlobal('fetch', vi.fn(async () =>
      Response.json({ status: 503, error: 'db down', retryInMs: 12000 }, { status: 503 })))
    const err = await api('/api/graph').catch((e) => e)
    expect(err).toBeInstanceOf(ApiError)
    expect(err.status).toBe(503)
    expect(err.retryInMs).toBe(12000)
  })

  it('treats a dead backend as status 0, not a crash', async () => {
    vi.stubGlobal('fetch', vi.fn(async () => {
      throw new TypeError('network down')
    }))
    const err = await api('/api/stats').catch((e) => e)
    expect(err.status).toBe(0)
  })
})
